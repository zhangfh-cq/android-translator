package cn.alsaces.translator.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper {
    private static final String DATABASE_NAME = "translator.db";
    private static final int DATABASE_VERSION = 1;

    private final DatabaseOpenHelper DatabaseOpenHelper;

    public DatabaseHelper(Context context) {
        DatabaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseOpenHelper getDatabaseOpenHelper() {
        return DatabaseOpenHelper;
    }

    public SQLiteDatabase getWritableDatabase() {
        return DatabaseOpenHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase() {
        return DatabaseOpenHelper.getReadableDatabase();
    }

}
