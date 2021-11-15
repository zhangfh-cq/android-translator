package cn.alsaces.translator.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String CREATE_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS history(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "origin_text text," +
            "translation text," +
            "type text)";

    private static final String CREATE_NEW_WORD_TABLE = "CREATE TABLE IF NOT EXISTS new_word(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "word text," +
            "translation text)";

    public DatabaseOpenHelper(@Nullable Context context, @Nullable String databaseName, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, databaseName, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建表
        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_NEW_WORD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
