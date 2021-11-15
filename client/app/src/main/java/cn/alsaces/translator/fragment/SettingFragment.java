package cn.alsaces.translator.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cn.alsaces.translator.R;
import cn.alsaces.translator.Translator;
import cn.alsaces.translator.database.DatabaseHelper;
import cn.alsaces.translator.util.NetUtil;
import cn.alsaces.translator.util.SPUtil;
import cn.alsaces.translator.view.CustomAlertDialog;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class SettingFragment extends Fragment {
    // 常量
    private static final String TAG = "SettingFragment";

    // View和Context
    private View view;
    private Context context;

    // 数据库
    private SQLiteDatabase sqLiteWritableDB;

    // 控件
    private Button voiceAutoTtsButton;
    private Button engAutoTtsTypeButton;
    private Button autoAddNewWordButton;
    private Button clearAllHistoryButton;
    private Button useHelpButton;
    private Button feedbackButton;
    private Button checkUpdateButton;
    private Button aboutButton;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        context = view.getContext();

        // 初始化
        initDatabase();
        initView();
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
        voiceAutoTtsButton = view.findViewById(R.id.setting_voice_auto_tts_button);
        engAutoTtsTypeButton = view.findViewById(R.id.setting_eng_auto_tts_type_button);
        autoAddNewWordButton = view.findViewById(R.id.setting_auto_add_new_word_button);
        clearAllHistoryButton = view.findViewById(R.id.setting_clear_all_history_button);
        useHelpButton = view.findViewById(R.id.setting_use_help_button);
        feedbackButton = view.findViewById(R.id.setting_feedback_button);
        checkUpdateButton = view.findViewById(R.id.setting_check_update_button);
        aboutButton = view.findViewById(R.id.setting_about_button);
    }

    // 初始化Listener
    private void initListener() {
        // 语音翻译自动播报译文按钮
        voiceAutoTtsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isVoiceAutoTts = SPUtil.getBoolean(Translator.CONFIG_NAME, Translator.VOICE_AUTO_TTS_CONFIG_NAME,
                        true, context);
                Log.d(TAG, "自动播报译文：" + String.valueOf(isVoiceAutoTts));
                String positiveHook = isVoiceAutoTts ? "  √" : "    ";
                String negativeHook = isVoiceAutoTts ? "    " : "  √";

                CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                customAlertDialog.setCustomTitle("自动播报")
                        .setCustomMessage("是否开启 \n" +
                                "语音翻译自动播报译文？")
                        .setCustomPositiveButton("开启" + positiveHook, new CustomAlertDialog.CustomOnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SPUtil.putBoolean(Translator.CONFIG_NAME, Translator.VOICE_AUTO_TTS_CONFIG_NAME, true, context);
                                Toast.makeText(context, "更改成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setCustomNegativeButton("关闭" + negativeHook, new CustomAlertDialog.CustomOnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SPUtil.putBoolean(Translator.CONFIG_NAME, Translator.VOICE_AUTO_TTS_CONFIG_NAME, false, context);
                                Toast.makeText(context, "更改成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        });

        // 英语自动播报发音类型按钮
        engAutoTtsTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String engAutoTtsType = SPUtil.getString(Translator.CONFIG_NAME, Translator.ENG_AUTO_TTS_TYPE_CONFIG_NAME,
                        "US", context);
                String USHook = engAutoTtsType.equals("US") ? "  √" : "    ";
                String UKHook = engAutoTtsType.equals("US") ? "    " : "  √";

                CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                customAlertDialog.setCustomTitle("发音类型")
                        .setCustomMessage("语音翻译 \n" +
                                "英语自动播报发音类型？")
                        .setCustomPositiveButton("美式" + USHook, new CustomAlertDialog.CustomOnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SPUtil.putString(Translator.CONFIG_NAME, Translator.ENG_AUTO_TTS_TYPE_CONFIG_NAME, "US", context);
                                Toast.makeText(context, "更改成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setCustomNegativeButton("英式" + UKHook, new CustomAlertDialog.CustomOnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SPUtil.putString(Translator.CONFIG_NAME, Translator.ENG_AUTO_TTS_TYPE_CONFIG_NAME, "UK", context);
                                Toast.makeText(context, "更改成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        });

        // 英语单词自动加入生词按钮
        autoAddNewWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAutoAddNewWord = SPUtil.getBoolean(Translator.CONFIG_NAME, Translator.AUTO_ADD_NEW_WORD_CONFIG_NAME,
                        true, context);
                String positiveHook = isAutoAddNewWord ? "  √" : "    ";
                String negativeHook = isAutoAddNewWord ? "    " : "  √";

                CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                customAlertDialog.setCustomTitle("自动生词")
                        .setCustomMessage("是否开启 \n" +
                                "英语单词自动加入生词？")
                        .setCustomPositiveButton("开启" + positiveHook, new CustomAlertDialog.CustomOnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SPUtil.putBoolean(Translator.CONFIG_NAME, Translator.AUTO_ADD_NEW_WORD_CONFIG_NAME, true, context);
                                Toast.makeText(context, "更改成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setCustomNegativeButton("关闭" + negativeHook, new CustomAlertDialog.CustomOnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SPUtil.putBoolean(Translator.CONFIG_NAME, Translator.AUTO_ADD_NEW_WORD_CONFIG_NAME, false, context);
                                Toast.makeText(context, "更改成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        });

        // 清空所有翻译历史记录按钮
        clearAllHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                customAlertDialog
                        .setCustomTitle("确认清空")
                        .setCustomMessage("是否清空 \n" +
                                "所有翻译历史记录？")
                        .setCustomPositiveButton("确认", new CustomAlertDialog.CustomOnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sqLiteWritableDB.execSQL("DELETE FROM history");
                                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setCustomNegativeButton("取消", null)
                        .create();

                customAlertDialog.show();
            }
        });

        // 翻译功能使用帮助按钮
        useHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                customAlertDialog
                        .setCustomTitle("使用帮助")
                        .setCustomMessage(
                                "文本翻译：\n" +
                                        "\b1.选择原文语种和译文语种\n" +
                                        "\b2.在输入框内输入原文文本\n" +
                                        "\b3.点击下方的翻译按钮即可\n\n" +
                                        "语音翻译：\n" +
                                        "\b1.选择原文语种和译文语种\n" +
                                        "\b2.按住下方语音按钮说原文\n" +
                                        "\b3.松开语音按钮等翻译结果\n\n" +
                                        "拍照翻译：\n" +
                                        "\b1.选择原文语种和译文语种\n" +
                                        "\b2.点击下方的拍照按钮拍照\n" +
                                        "\b3.拍照后点击确认等待结果"
                        )
                        .setCustomPositiveButton("确认", null)
                        .show();
                customAlertDialog.show();
            }
        });

        // 功能界面反馈建议按钮
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                customAlertDialog
                        .setCustomTitle("反馈建议")
                        .setCustomMessage(
                                "有任何建议可以发邮件到：\n" +
                                        "zhangfh_cq@163.com"
                        )
                        .setCustomPositiveButton("确认", null)
                        .show();

                customAlertDialog.show();
            }
        });

        // 检查软件更新按钮
        checkUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetUtil.isNetworkAvailable(context)) {
                    try {
                        JSONObject checkJsonObject = new JSONObject();
                        checkJsonObject.put("auth_key", Translator.VERSION_CHECK_AUTH_KEY);
                        String postData = checkJsonObject.toString();

                        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                        StringEntity postDataEntity = new StringEntity(postData, "UTF-8");

                        asyncHttpClient.post(context, Translator.VERSION_CHECK_API_URL, postDataEntity,
                                "charset=UTF-8", checkRepHandler);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "网络连接错误");
                    Toast.makeText(context, "网络似乎走丢了", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 关于翻译君按钮监听器
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                customAlertDialog
                        .setCustomTitle("关于")
                        .setCustomMessage("本软件为学习交流之作 \n" +
                                "可能随时跑路(bu shi)")
                        .setCustomPositiveButton("确认", null)
                        .show();

            }
        });
    }

    AsyncHttpResponseHandler checkRepHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try {
                JSONObject checkRepJsonObject = new JSONObject(new String(responseBody));
                int error_code = checkRepJsonObject.getInt("error_code");
                double newestVersion = checkRepJsonObject.getDouble("version");
                String log = checkRepJsonObject.getString("log");
                String downloadUrl = checkRepJsonObject.getString("url");

                int nowVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;

                if (error_code == 0) {
                    Toast.makeText(context, "检查成功", Toast.LENGTH_SHORT).show();
                    CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                    if (newestVersion > nowVersion) {
                        customAlertDialog
                                .setCustomTitle("检查更新")
                                .setCustomMessage(
                                        "有新版本:" + String.valueOf(newestVersion) + "\n\n" +
                                                "更新日志: \n" +
                                                log + "\n\n" +
                                                "下载地址：\n" +
                                                downloadUrl
                                )
                                .setCustomPositiveButton("前往下载", new CustomAlertDialog.CustomOnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Uri uri = Uri.parse(downloadUrl);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent);
                                    }
                                })
                                .show();
                    } else {
                        customAlertDialog
                                .setCustomTitle("检查更新")
                                .setCustomMessage("已是最新版本")
                                .setCustomPositiveButton("确认", null)
                                .show();
                    }
                    customAlertDialog.show();
                } else {
                    Log.e(TAG, "检查更新失败" + "响应数据为：" + checkRepJsonObject.toString());
                    Toast.makeText(context, "检查更新失败", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException | PackageManager.NameNotFoundException e) {
                Log.e(TAG, "检查更新失败");
                e.printStackTrace();
                Toast.makeText(context, "检查更新失败", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.e(TAG, "检查更新网络请求失败");
            error.printStackTrace();
            Toast.makeText(context, "检查更新失败", Toast.LENGTH_SHORT).show();
        }
    };
}
