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

import cn.alsaces.translator.R;
import cn.alsaces.translator.view.CustomAlertDialog;

public class HistoryUtil {
    private static final String TAG = "HistoryUtil";

    // 存储历史记录到数据库
    public static void storeHistoricalRecord(String originText, String translation, String historyType, SQLiteDatabase sqLiteWritableDB) {
        Cursor historyCursor = sqLiteWritableDB.rawQuery("SELECT * FROM history WHERE (origin_text=? AND translation=? AND type=?)",
                new String[]{originText, translation, historyType});
        if (historyCursor.moveToNext()) {
            int id = historyCursor.getInt(historyCursor.getColumnIndex("id"));
            sqLiteWritableDB.execSQL("DELETE FROM history WHERE (id=?)", new Object[]{id});
            Log.d(TAG, "重置历史记录：[" + historyType + "-" + originText + "-" + translation + "]");
        } else {
            Log.d(TAG, "新增历史记录：[" + historyType + "-" + originText + "-" + translation + "]");
        }
        historyCursor.close();
        sqLiteWritableDB.execSQL("INSERT INTO history (origin_text, translation, type) VALUES (?,?,?)",
                new String[]{originText, translation, historyType});
    }

    // 显示历史记录
    public static void showHistoricalRecords(Context context, String historyType, LinearLayout historySetLinearLayout, SQLiteDatabase sqLiteWritableDB) {
        // 删除所有历史记录按钮
        Button deleteAllHistoryButton = new Button(context);
        deleteAllHistoryButton.setText("删除所有历史记录");
        deleteAllHistoryButton.setTextSize(16);
        deleteAllHistoryButton.setTypeface(Typeface.DEFAULT_BOLD);
        deleteAllHistoryButton.setTextColor(context.getColor(R.color.white));
        deleteAllHistoryButton.setWidth(context.getResources().getDisplayMetrics().widthPixels);
        deleteAllHistoryButton.setBackgroundResource(R.drawable.common_button_selector);
        deleteAllHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                customAlertDialog
                        .setCustomTitle("确认删除")
                        .setCustomMessage("是否删除 \n" +
                                "所有历史记录？")
                        .setCustomPositiveButton("确认", new CustomAlertDialog.CustomOnClickListener() {
                            @Override
                            public void onClick(View v) {
                                historySetLinearLayout.removeAllViews();
                                historySetLinearLayout.addView(deleteAllHistoryButton);
                                sqLiteWritableDB.execSQL("DELETE FROM history WHERE (type=?)", new String[]{historyType});
                                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setCustomNegativeButton("取消", null)
                        .show();
            }
        });
        historySetLinearLayout.addView(deleteAllHistoryButton, 0);


        // 显示所有历史记录
        Cursor historyCursor = sqLiteWritableDB.rawQuery("SELECT * FROM history WHERE (type=?) ORDER BY id DESC", new String[]{historyType});
        while (historyCursor.moveToNext()) {
            // 获取原文和译文
            int id = historyCursor.getInt(historyCursor.getColumnIndex("id"));
            String originText = historyCursor.getString(historyCursor.getColumnIndex("origin_text"));
            String translation = historyCursor.getString(historyCursor.getColumnIndex("translation"));

            // 增加的控件
            TextView divisionTextView = new TextView(context);
            TextView historyTextView = new TextView(context);
            LinearLayout historyLinearLayout = new LinearLayout(context);

            // 历史记录TextView
            String history = originText + "\n\n" + translation;
            historyTextView.setText(history);
            historyTextView.setTextSize(16);
            historyTextView.setTypeface(Typeface.DEFAULT_BOLD);
            historyTextView.setTextColor(context.getColor(R.color.record_text_color));
            historyTextView.setWidth(context.getResources().getDisplayMetrics().widthPixels);
            historyTextView.setBackgroundResource(R.drawable.common_text_view_selector);
            historyTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                    customAlertDialog.setCustomTitle("选择操作")
                            .setCustomMessage("选择一个操作 \n" +
                                    "来对该历史记录进行")
                            .setCustomPositiveButton("复制", new CustomAlertDialog.CustomOnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, history));
                                    Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setCustomNegativeButton("删除", new CustomAlertDialog.CustomOnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    historySetLinearLayout.removeView(historyLinearLayout);
                                    sqLiteWritableDB.execSQL("DELETE FROM history WHERE (id=?)",
                                            new Object[]{id});
                                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                    return false;
                }
            });

            // 历史记录LinearLayout
            historyLinearLayout.addView(divisionTextView);
            historyLinearLayout.addView(historyTextView);
            historyLinearLayout.setOrientation(LinearLayout.VERTICAL);

            // 结果集LinearLayout加入该记录
            historySetLinearLayout.addView(historyLinearLayout);
        }
        historyCursor.close();
    }
}
