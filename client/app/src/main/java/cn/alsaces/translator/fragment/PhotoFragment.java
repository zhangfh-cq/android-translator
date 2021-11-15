package cn.alsaces.translator.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;
import androidx.fragment.app.Fragment;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.alsaces.translator.R;
import cn.alsaces.translator.Translator;
import cn.alsaces.translator.adapter.LanguageAdapter;
import cn.alsaces.translator.database.DatabaseHelper;
import cn.alsaces.translator.util.FileUtil;
import cn.alsaces.translator.util.LangUtil;
import cn.alsaces.translator.util.NetUtil;
import cn.alsaces.translator.util.NewWordUtil;
import cn.alsaces.translator.util.HistoryUtil;
import cn.alsaces.translator.view.CustomAlertDialog;
import cz.msebera.android.httpclient.Header;

public class PhotoFragment extends Fragment {
    // 常量
    private static final String HISTORY_RECORDE_TYPE = "photo";
    private static final String TAG = "PhotoFragment";
    private static final int CAMERA_REQUEST_CODE = 0x00000010;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;
    private static final boolean IS_MORE_THAN_ANDROID_P = Build.VERSION.SDK_INT > Build.VERSION_CODES.P;

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
    private CustomAlertDialog uploadTipDialog;

    // 语言
    private LanguageAdapter fromLangAdapter;
    private LanguageAdapter toLangAdapter;
    private String fromLangCode;
    private String toLangCode;
    private String newWordEnglish;
    private String newWordChinese;

    // 照片
    private Uri photoUri;
    private String photoPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_photo, container, false);
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
        fromLangSpinner = view.findViewById(R.id.photo_from_lang_spinner);
        toLangSpinner = view.findViewById(R.id.photo_to_lang_spinner);
        exchangeLangTextView = view.findViewById(R.id.photo_exchange_lang_text_view);
        translateButton = view.findViewById(R.id.photo_trans_button);
        historySetLinearLayout = view.findViewById(R.id.photo_history_set_linear_layout);

        // 显示历史记录
        HistoryUtil.showHistoricalRecords(context, HISTORY_RECORDE_TYPE, historySetLinearLayout, sqLiteWritableDB);
    }

    // 初始化语言
    private void initLang() {
        // 实例化Adapter
        fromLangAdapter = new LanguageAdapter(context, LangUtil.getPhotographicFromLangList());
        toLangAdapter = new LanguageAdapter(context, LangUtil.getPhotographicToLangList());

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

        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetUtil.isNetworkAvailable(context)) {
                    checkPermissionAndCamera();
                } else {
                    Log.e(TAG, "网络连接错误");
                    Toast.makeText(context, "网络似乎走丢了", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 检查权限并调起相机
    private void checkPermissionAndCamera() {
        Activity activity = getActivity();
        if (activity != null) {
            int hasCameraPermission = ContextCompat.checkSelfPermission(activity.getApplication(), Manifest.permission.CAMERA);
            if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                this.requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
            }
        } else {
            Log.e(TAG, "获取Activity失败");
        }
    }

    // 权限申请的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openCamera();
            } else {
                Log.e(TAG, "未授予相机权限");
                Toast.makeText(context, "请授予相机权限后再试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 调用相机
    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 是否有相机
        if (captureIntent.resolveActivity(context.getPackageManager()) != null) {
            if (IS_MORE_THAN_ANDROID_P) {
                photoUri = createPhotoUri();
            } else {
                File photoFile = null;
                try {
                    photoFile = createPhotoFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (photoFile != null) {
                    photoPath = photoFile.getAbsolutePath();
                    // 通过FileProvider创建content类型的Uri
                    photoUri = FileProvider.getUriForFile(context, context.getPackageName() + ".file-provider", photoFile);
                }
            }

            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
            } else {
                Log.e(TAG, "URI为空");
                Toast.makeText(context, "图片创建失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "未找到相机！");
            Toast.makeText(context, "未发现相机", Toast.LENGTH_SHORT).show();
        }
    }

    // 创建图片URI
    private Uri createPhotoUri() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    // 创建图片File
    private File createPhotoFile() throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        if (!storageDir.exists()) {
            if (storageDir.mkdirs()) {
                Log.d(TAG, "创建图片存储路径成功");
            } else {
                Log.e(TAG, "创建图片存储路径失败");
            }
        } else {
            Log.d(TAG, "图片存储路径已存在");
        }

        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            Log.e(TAG, "设备存储状态异常");
            return null;
        } else {
            return tempFile;
        }
    }


    // 相机返回的数据
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (IS_MORE_THAN_ANDROID_P) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        photoPath = FileUtil.getPathFromUri(photoUri, activity.getContentResolver());
                    } else {
                        Log.e(TAG, "获取Activity失败");
                    }
                } else {
                    // 图片路径已存在
                }
                Log.d(TAG, "图片保存路径：" + photoPath);

                // 上传提示
                uploadTipDialog = new CustomAlertDialog(context);
                uploadTipDialog.setCustomTitle("提示")
                        .setCustomProgressBar(true)
                        .setCustomMessage("图片上传中，请稍后......")
                        .setCustomPositiveButton("关闭", null)
                        .show();

                // 翻译
                translate(photoPath, fromLangCode, toLangCode, transRepHandler);
            } else {
                Toast.makeText(context, "取消拍照", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void translate(String photoPath, String fromLang, String toLang, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        // 压缩图片
        Tiny.getInstance()
                .source(photoPath)
                .asFile()
                .withOptions(new Tiny.FileCompressOptions())
                .compress(new FileCallback() {
                    @Override
                    public void callback(boolean isSuccess, String outfile, Throwable t) {
                        File imageFile = null;
                        if (isSuccess) {
                            imageFile = new File(outfile);
                            Log.d(TAG, "图片压缩成功");
                        } else {
                            imageFile = new File(photoPath);
                            Log.w(TAG, "图片压缩失败");
                            t.printStackTrace();
                        }

                        Log.d(TAG,"开始翻译");
                        String url = Translator.IMAGE_TRANS_API_URL + "index.php?auth_key=" + Translator.IMAGE_TRANS_API_AUTH + "&from=" + fromLang + "&to=" + toLang;
                        RequestParams reqParam = new RequestParams();
                        try {
                            reqParam.put("img", imageFile);
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "未找到图片文件");
                            e.printStackTrace();
                        }

                        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                        asyncHttpClient.post(url, reqParam, asyncHttpResponseHandler);
                    }
                });
    }

    AsyncHttpResponseHandler transRepHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            uploadTipDialog.cancel();
            try {
                JSONObject transRepJsonObject = new JSONObject(new String(responseBody));
                Log.d(TAG, "图片翻译结果为：" + transRepJsonObject.toString());

                String errorCode = transRepJsonObject.getString("error_code");
                switch (errorCode) {
                    case "0":
                        String repFromLangCode = transRepJsonObject.getJSONObject("data").getString("from");
                        String repToLangCode = transRepJsonObject.getJSONObject("data").getString("to");
                        String originText = transRepJsonObject.getJSONObject("data").getString("sumSrc");
                        String translation = transRepJsonObject.getJSONObject("data").getString("sumDst");

                        // 存入历史记录到数据库
                        HistoryUtil.storeHistoricalRecord(originText, translation, HISTORY_RECORDE_TYPE, sqLiteWritableDB);
                        // 记录增加，视图更新
                        historySetLinearLayout.removeAllViews();
                        HistoryUtil.showHistoricalRecords(context, HISTORY_RECORDE_TYPE, historySetLinearLayout, sqLiteWritableDB);
                        Toast.makeText(context, "翻译成功", Toast.LENGTH_SHORT).show();

                        // 判断生词
                        if (NewWordUtil.isMeetNewWordCondition(repFromLangCode, repToLangCode, context)) {
                            newWordEnglish = toLangCode.equals("en") ? translation : originText;
                            newWordChinese = toLangCode.equals("en") ? originText : translation;
                            NewWordUtil.judgeLegalWord(newWordEnglish, context, wordJudgeRepHandler);
                        } else {
                            Log.d(TAG, "未满足生词判断条件");
                        }
                        break;
                    case "69003":
                        Log.e(TAG, "内容识别失败");
                        Toast.makeText(context, "内容识别失败", Toast.LENGTH_SHORT).show();
                        break;
                    case "69004":
                        Log.w(TAG, "识别内容为空");
                        Toast.makeText(context, "识别内容为空", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Log.e(TAG, "翻译失败，响应数据为：" + transRepJsonObject.toString());
                        Toast.makeText(context, "翻译失败，请稍后重试", Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            uploadTipDialog.cancel();
            Log.e(TAG, "拍照翻译网络请求失败！");
            error.printStackTrace();
            Toast.makeText(context, "翻译失败，请稍后重试", Toast.LENGTH_SHORT).show();
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
            error.printStackTrace();
            Log.e(TAG, "生词判断请求失败");
            Toast.makeText(context, "生词判断失败", Toast.LENGTH_SHORT).show();
        }
    };
}
