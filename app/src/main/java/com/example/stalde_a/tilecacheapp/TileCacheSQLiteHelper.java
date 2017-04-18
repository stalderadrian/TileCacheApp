package com.example.stalde_a.tilecacheapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.File;

public class TileCacheSQLiteHelper extends SQLiteOpenHelper {

    public static final String TAG = TileCacheSQLiteHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "tilecacheapp.cache";
    public static final int DATABASE_VERSION = 1;
    public static final String[] DATABASE_CREATE = {
            "CREATE TABLE 'CACHED_TILE' (\n" +
                    "'X' INTEGER NOT NULL,\n" +
                    " PRIMARY KEY (X)\n" +
                    ");"
    };
    private Context mContext;

    public TileCacheSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        for (String sql : DATABASE_CREATE) {
            try {
                database.execSQL(sql);
            } catch (SQLException e) {
                Log.w(TAG, e);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public SQLiteDatabase openDatabase(File directory) throws Exception {
        return null;
    }

    public SQLiteDatabase getReadonlyDatabase(final File directory) {
        ContextWrapper wrappedContext = new ContextWrapper(mContext.getApplicationContext()) {

            @Override
            public SQLiteDatabase openOrCreateDatabase(
                    String name, int mode, SQLiteDatabase.CursorFactory factory) {
                return SQLiteDatabase.openDatabase(getDatabasePath(name).getPath(), factory, SQLiteDatabase.OPEN_READONLY);
            }

            @Override
            public SQLiteDatabase openOrCreateDatabase(
                    String name, int mode, SQLiteDatabase.CursorFactory factory,
                    DatabaseErrorHandler errorHandler) {
                return SQLiteDatabase.openDatabase(getDatabasePath(name).getPath(), factory, SQLiteDatabase.OPEN_READONLY, errorHandler);
            }

            @Override
            public File getDatabasePath(String name) {
                return new File(directory, name);
            }
        };

        TileCacheSQLiteHelper helper = new TileCacheSQLiteHelper(wrappedContext);
        return helper.getReadableDatabase();
    }

    public SQLiteDatabase getWritableDatabase(final File directory) {
        ContextWrapper wrappedContext = new ContextWrapper(mContext.getApplicationContext()) {

            @Override
            public SQLiteDatabase openOrCreateDatabase(
                    String name, int mode, SQLiteDatabase.CursorFactory factory) {
                return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name).getPath(), factory);
            }

            @Override
            public SQLiteDatabase openOrCreateDatabase(
                    String name, int mode, SQLiteDatabase.CursorFactory factory,
                    DatabaseErrorHandler errorHandler) {
                return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name).getPath(), factory, errorHandler);
            }

            @Override
            public File getDatabasePath(String name) {
                return new File(directory, name);
            }
        };

        TileCacheSQLiteHelper helper = new TileCacheSQLiteHelper(wrappedContext);
        return helper.getWritableDatabase();
    }
}
