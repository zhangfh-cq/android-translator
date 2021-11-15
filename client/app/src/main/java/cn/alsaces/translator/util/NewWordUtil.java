package cn.alsaces.translator.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cn.alsaces.translator.Translator;
import cn.alsaces.translator.R;
import cn.alsaces.translator.view.CustomAlertDialog;
import cz.msebera.android.httpclient.entity.StringEntity;

public class NewWordUtil {
    private static final String TAG = "NewWordUtil";

    // 获取是否满足生词条件
    public static boolean isMeetNewWordCondition(String fromLangCode, String toLangCode, Context context) {
        boolean isAutoAddNewWord = SPUtil.getBoolean(Translator.CONFIG_NAME,
                Translator.AUTO_ADD_NEW_WORD_CONFIG_NAME, true, context);

        boolean isChiToEng = fromLangCode.equals("zh") && toLangCode.equals("en");
        boolean isEngToChi = fromLangCode.equals("en") && toLangCode.equals("zh");

        return isAutoAddNewWord && (isChiToEng || isEngToChi);
    }

    //生词判断
    public static void judgeLegalWord(String word, Context context, AsyncHttpResponseHandler asyncHttpResponseHandler) throws JSONException {
        JSONObject judgeReqJsonObject = new JSONObject();
        judgeReqJsonObject.put("auth_key", Translator.LEGAL_WORD_AUTH_KEY);
        judgeReqJsonObject.put("word", word);
        String postData = judgeReqJsonObject.toString();

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        StringEntity postDataEntity = new StringEntity(postData, "UTF-8");

        asyncHttpClient.post(context, Translator.LEGAL_WORD_API_URL, postDataEntity, "charset=UTF-8", asyncHttpResponseHandler);
    }

    // 存储生词记录到数据库
    public static void storeNewWord(String english, String chinese, SQLiteDatabase sqLiteWritableDB) {
        english = english.toLowerCase();
        Cursor newWordCursor = sqLiteWritableDB.rawQuery("SELECT * FROM new_word WHERE (word=?)", new String[]{english});
        if (newWordCursor.moveToNext()) {
            Log.d(TAG, "生词[" + english + ":" + chinese + "]已经存在");
        } else {
            sqLiteWritableDB.execSQL("INSERT INTO new_word(word,translation) VALUES (?,?)", new String[]{english, chinese});
            Log.d(TAG, "生词[" + english + ":" + chinese + "]已经存入数据库");
        }
        newWordCursor.close();
    }

    // 显示生词记录
    public static void showNewWordRecord(Context context, LinearLayout newWordSetLinearLayout, SQLiteDatabase sqLiteWritableDB) {
        // 删除所有生词按钮
        Button deleteAllNewWordButton = new Button(context);
        deleteAllNewWordButton.setText("删除所有生词");
        deleteAllNewWordButton.setTextSize(16);
        deleteAllNewWordButton.setTypeface(Typeface.DEFAULT_BOLD);
        deleteAllNewWordButton.setTextColor(context.getColor(R.color.white));
        deleteAllNewWordButton.setWidth(context.getResources().getDisplayMetrics().widthPixels);
        deleteAllNewWordButton.setBackgroundResource(R.drawable.common_button_selector);
        deleteAllNewWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                customAlertDialog
                        .setCustomTitle("确认删除")
                        .setCustomMessage("是否删除 \n" +
                                "所有生词记录？")
                        .setCustomPositiveButton("确认", new CustomAlertDialog.CustomOnClickListener() {
                            @Override
                            public void onClick(View v) {
                                newWordSetLinearLayout.removeAllViews();
                                newWordSetLinearLayout.addView(deleteAllNewWordButton);
                                sqLiteWritableDB.execSQL("DELETE FROM new_word");
                                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setCustomNegativeButton("取消", null)
                        .show();
            }
        });
        newWordSetLinearLayout.addView(deleteAllNewWordButton, 0);


        // 显示所有生词
        Cursor newWordCursor = sqLiteWritableDB.rawQuery("SELECT * FROM new_word ORDER BY id DESC", new String[]{});
        while (newWordCursor.moveToNext()) {
            // 获取原文和译文
            int id = newWordCursor.getInt(newWordCursor.getColumnIndex("id"));
            String originText = newWordCursor.getString(newWordCursor.getColumnIndex("word"));
            String translation = newWordCursor.getString(newWordCursor.getColumnIndex("translation"));

            // 增加的控件
            TextView divisionTextView = new TextView(context);
            TextView newWordTextView = new TextView(context);
            LinearLayout newWordLinearLayout = new LinearLayout(context);

            // 历史记录TextView
            String newWordRecord = originText + "\n\n" + translation;
            newWordTextView.setText(newWordRecord);
            newWordTextView.setTextSize(16);
            newWordTextView.setTypeface(Typeface.DEFAULT_BOLD);
            newWordTextView.setTextColor(context.getColor(R.color.record_text_color));
            newWordTextView.setWidth(context.getResources().getDisplayMetrics().widthPixels);
            newWordTextView.setBackgroundResource(R.drawable.common_text_view_selector);
            newWordTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                    customAlertDialog.setCustomTitle("选择操作")
                            .setCustomMessage("选择一个操作 \n" +
                                    "来对该生词记录进行")
                            .setCustomPositiveButton("复制", new CustomAlertDialog.CustomOnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, newWordRecord));
                                    Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setCustomNegativeButton("删除", new CustomAlertDialog.CustomOnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    newWordSetLinearLayout.removeView(newWordLinearLayout);
                                    sqLiteWritableDB.execSQL("DELETE FROM new_word WHERE (id=?)", new Object[]{id});
                                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                    return false;
                }
            });


            // 生词LinearLayout
            newWordLinearLayout.addView(divisionTextView);
            newWordLinearLayout.addView(newWordTextView);
            newWordLinearLayout.setOrientation(LinearLayout.VERTICAL);

            // 结果集LinearLayout加入该生词
            newWordSetLinearLayout.addView(newWordLinearLayout);
        }
        newWordCursor.close();
    }
}
