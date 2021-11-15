package cn.alsaces.translator.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.baidu.translate.asr.OnRecognizeListener;
import com.baidu.translate.asr.TransAsrClient;
import com.baidu.translate.asr.TransAsrConfig;
import com.baidu.translate.asr.data.Language;
import com.baidu.translate.asr.data.RecognitionResult;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cn.alsaces.translator.R;
import cn.alsaces.translator.Translator;
import cn.alsaces.translator.database.DatabaseHelper;
import cn.alsaces.translator.adapter.VoiceLangAdapter;
import cn.alsaces.translator.util.NetUtil;
import cn.alsaces.translator.util.NewWordUtil;
import cn.alsaces.translator.util.HistoryUtil;
import cn.alsaces.translator.util.SPUtil;
import cz.msebera.android.httpclient.Header;

public class VoiceFragment extends Fragment {
    // 常量
    private static final String HISTORY_RECORDE_TYPE = "voice";
    private static final String TAG = "VoiceFragment";
    private static final int PERMISSION_REQUEST_CODE = 2021;

    // View和Context
    private View view;
    private Context context;

    // 数据库
    private SQLiteDatabase sqLiteWritableDB;

    // 控件
    private Spinner fromLangSpinner;
    private Spinner toLangSpinner;
    private TextView exchangeLangTextView;
    private LinearLayout historySetLinearLayout;
    private Button translateButton;

    // 语言
    private VoiceLangAdapter fromLangAdapter;
    private VoiceLangAdapter toLangAdapter;
    private Language fromLang;
    private Language toLang;
    private String newWordEnglish;
    private String newWordChinese;

    // 权限
    private boolean hasPermission;

    // 语音翻译
    private TransAsrClient transAsrClient;
    private TransAsrConfig transAsrConfig;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_voice, container, false);
        context = view.getContext();

        // 初始化
        initDatabase();
        initView();
        initLang();
        initListener();

        checkPermission();
        return view;
    }

    // 初始化Database
    private void initDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        sqLiteWritableDB = databaseHelper.getWritableDatabase();
    }

    // 初始化View
    private void initView() {
        fromLangSpinner = view.findViewById(R.id.voice_from_lang_spinner);
        toLangSpinner = view.findViewById(R.id.voice_to_lang_spinner);
        exchangeLangTextView = view.findViewById(R.id.voice_exchange_lang_text_view);
        historySetLinearLayout = view.findViewById(R.id.voice_history_set_linear_layout);
        translateButton = view.findViewById(R.id.voice_trans_button);

        // 显示历史记录
        HistoryUtil.showHistoricalRecords(context, HISTORY_RECORDE_TYPE, historySetLinearLayout, sqLiteWritableDB);
    }

    // 初始化语言
    private void initLang() {
        // 实例化Adapter
        fromLangAdapter = new VoiceLangAdapter(context, Language.getAsrAvailableLanguages());
        toLangAdapter = new VoiceLangAdapter(context, Language.values());

        // 设置Spinner控件的Adapter
        fromLangSpinner.setAdapter(fromLangAdapter);
        toLangSpinner.setAdapter(toLangAdapter);

        // 设置默认的语言
        fromLang = Language.Chinese;
        toLang = Language.English;

        // 设置默认的Item
        fromLangSpinner.setSelection(0);
        toLangSpinner.setSelection(1);
    }

    // 初始化Listener
    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        fromLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLang = fromLangAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        toLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLang = toLangAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        exchangeLangTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toLangSpinner.getSelectedItemPosition() > 3) {
                    Toast.makeText(context, "超出源语言支持范围", Toast.LENGTH_SHORT).show();
                } else {
                    int fromLangSelectedItemPosition = fromLangSpinner.getSelectedItemPosition();
                    int toLangSelectedItemPosition = toLangSpinner.getSelectedItemPosition();
                    fromLangSpinner.setSelection(toLangSelectedItemPosition);
                    toLangSpinner.setSelection(fromLangSelectedItemPosition);
                }
            }
        });

        translateButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (NetUtil.isNetworkAvailable(context)) {
                            v.setPressed(true);
                            if (hasPermission) {
                                startRecognize();
                            } else {
                                Log.e(TAG, "未授予应用权限");
                                Toast.makeText(context, "请授予应用权限", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "网络连接错误");
                            Toast.makeText(context, "网络似乎走丢了", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (hasPermission) {
                            stopRecognize();
                        } else {
                            // 未授予权限
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    // 开始语音识别
    private void startRecognize() {
        Log.d(TAG, "开始语音识别");
        // 是否回调中间结果
        transAsrConfig.setPartialCallbackEnabled(false);
        // 是否自动播报支持语言的译文
        boolean isAutoTts = SPUtil.getBoolean(Translator.CONFIG_NAME, Translator.VOICE_AUTO_TTS_CONFIG_NAME, true, context);
        transAsrConfig.setAutoPlayTts(isAutoTts);
        // 英语的发音类型
        int engTtsType = SPUtil.getString(Translator.CONFIG_NAME, Translator.ENG_AUTO_TTS_TYPE_CONFIG_NAME, "US", context)
                .equals("US") ? TransAsrConfig.TTS_ENGLISH_TYPE_US : TransAsrConfig.TTS_ENGLISH_TYPE_UK;
        transAsrConfig.setTtsEnglishType(engTtsType);
        // 开始识别提示音
        transAsrConfig.setRecognizeStartAudioRes(R.raw.speech_recognition_start_audio);
        // 刷新Config
        transAsrClient.setConfig(transAsrConfig);
        // 开始语音识别
        transAsrClient.startRecognize(fromLang, toLang);
    }

    // 取消语音识别
    private void stopRecognize() {
        Log.d(TAG, "语音识别结束");
        // 停止语音识别（有回调）
        transAsrClient.stopRecognize();
        // 取消语音识别（没有回调）
        // transAsrClient.cancelRecognize();
    }


    // 检查权限
    private void checkPermission() {
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        this.requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    // 检查权限的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allowed = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allowed = false;
                    break;
                }
            }
            if (allowed) {
                hasPermission = true;
                initTransAsrClient();
            } else {
                hasPermission = false;
                Toast.makeText(context, "未授予应用相关权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 初始化语音翻译Client
    private void initTransAsrClient() {
        transAsrConfig = new TransAsrConfig(Translator.BAIDU_TRANS_APP_ID, Translator.BAIDU_TRANS_SECRET_KEY);
        transAsrClient = new TransAsrClient(context, transAsrConfig);

        // 设置回调
        transAsrClient.setRecognizeListener(new OnRecognizeListener() {
            @Override
            public void onRecognized(int resultType, @NonNull RecognitionResult result) {
                if (resultType == OnRecognizeListener.TYPE_PARTIAL_RESULT) { // 中间结果
                    Log.d(TAG, "中间识别结果：" + result.getAsrResult());
                } else if (resultType == OnRecognizeListener.TYPE_FINAL_RESULT) { // 最终结果
                    if (result.getError() == 0) {
                        Log.d(TAG, "识别结果：" + result.getAsrResult());
                        Log.d(TAG, "翻译结果：" + result.getTransResult());

                        // 存入历史记录
                        HistoryUtil.storeHistoricalRecord(result.getAsrResult(), result.getTransResult(),
                                HISTORY_RECORDE_TYPE, sqLiteWritableDB);

                        // 更新历史记录
                        historySetLinearLayout.removeAllViews();
                        HistoryUtil.showHistoricalRecords(context, HISTORY_RECORDE_TYPE, historySetLinearLayout, sqLiteWritableDB);
                        Toast.makeText(context, "翻译成功", Toast.LENGTH_SHORT).show();

                        // 判断生词
                        if (NewWordUtil.isMeetNewWordCondition(fromLang.getAbbreviation(), toLang.getAbbreviation(), context)) {
                            newWordEnglish = toLang.getAbbreviation().equals("en") ? result.getTransResult() : result.getAsrResult();
                            newWordChinese = toLang.getAbbreviation().equals("en") ? result.getAsrResult() : result.getTransResult();
                            try {
                                NewWordUtil.judgeLegalWord(newWordEnglish, context, wordJudgeRepHandler);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "未满足生词判断条件");
                        }
                    } else if (result.getError() == 58001) {
                        Log.e(TAG, "译文语言方向不支持" + result.getErrorMsg());
                        Toast.makeText(context, "译文语言方向不支持", Toast.LENGTH_SHORT).show();
                    } else if (result.getError() == 607001) {
                        Log.e(TAG, "识别结果为空,详细信息：" + result.getErrorMsg());
                        Toast.makeText(context, "识别结果为空", Toast.LENGTH_SHORT).show();
                    } else if (result.getError() == 608001) {
                        Log.e(TAG, "语音翻译繁忙,详细信息：" + result.getErrorMsg());
                        Toast.makeText(context, "语音翻译繁忙", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "翻译失败,详细信息：" + result.getErrorMsg());
                        Toast.makeText(context, "翻译失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
    }

    AsyncHttpResponseHandler wordJudgeRepHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try {
                JSONObject judgeRepJsonObject = new JSONObject(new String(responseBody));
                if (judgeRepJsonObject.getInt("error_code") == 0) {
                    boolean isLegalWord = judgeRepJsonObject.getBoolean("legal");

                    if (isLegalWord) {
                        NewWordUtil.storeNewWord(newWordEnglish, newWordChinese, sqLiteWritableDB);
                    } else {
                        Log.d(TAG, "[" + newWordEnglish + "]不是合法单词");
                    }
                } else {
                    Log.e(TAG, "生词判断失败，响应数据为：" + judgeRepJsonObject.toString());
                    Toast.makeText(context, "生词判断失败", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.e(TAG, "生词判断请求失败");
            error.printStackTrace();
            Toast.makeText(context, "生词判断失败", Toast.LENGTH_SHORT).show();
        }
    };

}