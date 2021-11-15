package cn.alsaces.translator.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cn.alsaces.translator.R;
import cn.alsaces.translator.Translator;
import cn.alsaces.translator.adapter.LanguageAdapter;
import cn.alsaces.translator.database.DatabaseHelper;
import cn.alsaces.translator.util.LangUtil;
import cn.alsaces.translator.util.NetUtil;
import cn.alsaces.translator.util.NewWordUtil;
import cn.alsaces.translator.util.HistoryUtil;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class TextFragment extends Fragment {
    // 常量
    private static final String TAG = "TextFragment";
    private static final String HISTORY_RECORDE_TYPE = "text";

    // View和Context
    private View view;
    private Context context;

    // 数据库
    private SQLiteDatabase sqLiteWritableDB;

    // 控件
    private Spinner fromLangSpinner;
    private Spinner toLangSpinner;
    private TextView exchangeLangTextView;
    private EditText inputEditText;
    private TextView clearInputTextView;
    private Button translateButton;
    private ScrollView historySetScrollview;
    private LinearLayout historySetLinearLayout;

    // 语言
    private LanguageAdapter fromLangAdapter;
    private LanguageAdapter toLangAdapter;
    private String fromLangCode;
    private String toLangCode;
    private String fromLangInputText;
    private String newWordEnglish;
    private String newWordChinese;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_text, container, false);
        context = view.getContext();

        // 初始化
        initDatabase();
        initView();
        initLang();
        initListener();

        return view;
    }

    // 初始化Database
    private void initDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        sqLiteWritableDB = databaseHelper.getWritableDatabase();
    }

    // 初始化View
    private void initView() {
        fromLangSpinner = view.findViewById(R.id.text_from_lang_spinner);
        toLangSpinner = view.findViewById(R.id.text_to_lang_spinner);
        exchangeLangTextView = view.findViewById(R.id.text_exchange_lang_text_view);
        inputEditText = view.findViewById(R.id.text_input_edit_text);
        clearInputTextView = view.findViewById(R.id.text_clear_input_text_view);
        translateButton = view.findViewById(R.id.text_translate_button);
        historySetScrollview = view.findViewById(R.id.text_history_set_scroll_view);
        historySetLinearLayout = view.findViewById(R.id.text_history_set_linear_layout);

        // 显示历史记录
        HistoryUtil.showHistoricalRecords(context, HISTORY_RECORDE_TYPE, historySetLinearLayout, sqLiteWritableDB);
    }

    // 初始化语言
    private void initLang() {
        // 实例化Adapter
        fromLangAdapter = new LanguageAdapter(context, LangUtil.getTextualFromLangList());
        toLangAdapter = new LanguageAdapter(context, LangUtil.getTextualToLangList());

        // 设置Spinner控件的Adapter
        fromLangSpinner.setAdapter(fromLangAdapter);
        toLangSpinner.setAdapter(toLangAdapter);

        // 设置默认的Code
        fromLangCode = "auto";
        toLangCode = "en";

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
                fromLangCode = fromLangAdapter.getItem(position).entrySet().iterator().next().getValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        toLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLangCode = toLangAdapter.getItem(position).entrySet().iterator().next().getValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fromLangInputText = String.valueOf(s).trim();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        exchangeLangTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromLangCode.equals("auto")) {
                    Toast.makeText(context, "源语言为自动检测无法交换", Toast.LENGTH_SHORT).show();
                } else {
                    int fromLangSelectedItemPosition = fromLangSpinner.getSelectedItemPosition();
                    int toLangSelectedItemPosition = toLangSpinner.getSelectedItemPosition();
                    fromLangSpinner.setSelection(toLangSelectedItemPosition + 1);
                    toLangSpinner.setSelection(fromLangSelectedItemPosition - 1);
                }
            }
        });

        inputEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                inputEditText.setMinLines(6);
                inputEditText.setMaxLines(6);
                clearInputTextView.setVisibility(View.VISIBLE);
                translateButton.setVisibility(View.VISIBLE);
                return false;
            }
        });

        clearInputTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputEditText.setText("");
            }
        });

        historySetScrollview.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                inputEditText.setMinLines(4);
                inputEditText.setMaxLines(4);
                clearInputTextView.setVisibility(View.GONE);
                translateButton.setVisibility(View.GONE);

                // 收回软键盘
                Activity activity = getActivity();
                if (activity != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
                    } else {
                        Log.e(TAG, "获取键盘服务失败");
                    }
                } else {
                    Log.e(TAG, "获取Activity失败");
                }

                return false;
            }
        });

        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromLangInputText.isEmpty()) {  // 判断空值
                    Log.e(TAG, "输入的文本为空");
                    Toast.makeText(context, "输入文本不能为空", Toast.LENGTH_SHORT).show();
                } else if (!NetUtil.isNetworkAvailable(context)) {  // 检查网络
                    Log.e(TAG, "网络连接错误");
                    Toast.makeText(context, "网络似乎走丢了", Toast.LENGTH_SHORT).show();
                } else {
                    // 开始翻译
                    Log.d(TAG, "输入：" + fromLangInputText + "，源语言：" + fromLangCode + "，目标语言：" + toLangCode);
                    try {
                        JSONObject transReqJsonObject = new JSONObject();
                        transReqJsonObject.put("auth_key", Translator.TEXT_TRANS_AUTH_KEY);
                        transReqJsonObject.put("query", fromLangInputText);
                        transReqJsonObject.put("from", fromLangCode);
                        transReqJsonObject.put("to", toLangCode);
                        String postData = transReqJsonObject.toString();

                        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                        StringEntity postDataEntity = new StringEntity(postData, "UTF-8");

                        asyncHttpClient.post(context, Translator.TEXT_TRANS_API_URL, postDataEntity,
                                "charset=UTF-8", transRepHandler);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    AsyncHttpResponseHandler transRepHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try {
                // 获取翻译结果
                JSONObject transRepJsonObject = new JSONObject(new String(responseBody));
                if (!transRepJsonObject.has("error_code")) {
                    String repFromLangCode = transRepJsonObject.getString("from");
                    String repToLangCode = transRepJsonObject.getString("to");
                    String translation = transRepJsonObject.getJSONArray("trans_result").getJSONObject(0).getString("dst");

                    // 存入历史记录
                    HistoryUtil.storeHistoricalRecord(fromLangInputText, translation, HISTORY_RECORDE_TYPE, sqLiteWritableDB);
                    // 更新历史记录
                    historySetLinearLayout.removeAllViews();
                    HistoryUtil.showHistoricalRecords(context, HISTORY_RECORDE_TYPE, historySetLinearLayout, sqLiteWritableDB);
                    Toast.makeText(context, "翻译成功", Toast.LENGTH_SHORT).show();

                    // 判断生词
                    if (NewWordUtil.isMeetNewWordCondition(repFromLangCode, repToLangCode, context)) {
                        newWordEnglish = toLangCode.equals("en") ? translation : fromLangInputText;
                        newWordChinese = toLangCode.equals("en") ? fromLangInputText : translation;
                        NewWordUtil.judgeLegalWord(newWordEnglish, context, wordJudgeRepHandler);
                    } else {
                        Log.d(TAG, "未满足生词判断条件");
                    }
                } else if (transRepJsonObject.getString("error_code").equals("54003")) {
                    Log.e(TAG, "文本翻译繁忙！" + transRepJsonObject.toString());
                    Toast.makeText(context, "文本翻译繁忙", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "翻译失败，响应数据为：" + transRepJsonObject.toString());
                    Toast.makeText(context, "翻译失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.e(TAG, "网络请求失败");
            error.printStackTrace();
            Toast.makeText(context, "翻译失败，请稍后重试", Toast.LENGTH_LONG).show();
        }

    };

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
