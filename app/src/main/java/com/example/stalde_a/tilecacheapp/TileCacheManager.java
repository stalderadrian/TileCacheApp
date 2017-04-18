package com.example.stalde_a.tilecacheapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TileCacheManager {

    public static final String TILE_CACHE_ROOT_FOLDER_NAME = "TileCacheApp";

    private final InternalStorageTileCacheAccess mInternalStorageTileCacheAccess;
    private final SdCardTileCacheAccess mSdCardTileCacheAccess;
    private SdCardAccess mSdCardAccess;

    public TileCacheManager(Context context, SdCardAccess sdCardAccess) {
        mSdCardAccess = sdCardAccess;
        TileCacheSQLiteHelper tileCacheSQLiteHelper = new TileCacheSQLiteHelper(context);
        mSdCardTileCacheAccess = new SdCardTileCacheAccess(sdCardAccess, tileCacheSQLiteHelper);
        mInternalStorageTileCacheAccess = new InternalStorageTileCacheAccess(context, tileCacheSQLiteHelper);
    }

    public TileCache create(String name, Boolean onSdCard) throws Exception {
        if (onSdCard) {
            return mSdCardTileCacheAccess.create(name);
        } else {
            return mInternalStorageTileCacheAccess.create(name);
        }
    }

    public List<TileCache> getTileCaches() throws Exception {
        List<TileCache> tileCaches = new ArrayList<>();
        tileCaches.addAll(mInternalStorageTileCacheAccess.getTileCaches());
        tileCaches.addAll(mSdCardTileCacheAccess.getTileCaches());
        return tileCaches;
    }

    public void delete(TileCache tileCache) throws Exception {
        if (tileCache.isOnSdCard()) {
            mSdCardTileCacheAccess.delete(tileCache);
        } else {
            mInternalStorageTileCacheAccess.delete(tileCache);
        }
    }

    public SQLiteDatabase getReadonlyDatabase(TileCache tileCache) throws Exception {
        if (tileCache.isOnSdCard()) {
            return mSdCardTileCacheAccess.getReadonlyDatabase(tileCache);
        } else {
            return mInternalStorageTileCacheAccess.getWritableDatabase(tileCache);
        }
    }

    public SQLiteDatabase getWritableDatabase(TileCache tileCache) throws Exception {
        if (tileCache.isOnSdCard()) {
            return mSdCardTileCacheAccess.getWritableDatabase(tileCache);
        } else {
            return mInternalStorageTileCacheAccess.getWritableDatabase(tileCache);
        }
    }

    public int getNumberOfX(TileCache tileCache) throws Exception {
        SQLiteDatabase database = getReadonlyDatabase(tileCache);

        Cursor cursor      = database.rawQuery("SELECT  * FROM 'CACHED_TILE'", null);
        int numberOfX = cursor.getCount();
        cursor.close();
        database.close();
        return numberOfX;
    }

    public void addX(TileCache tileCache) throws Exception {
        SQLiteDatabase database = getWritableDatabase(tileCache);

        Cursor cursor      = database.rawQuery("SELECT  * FROM 'CACHED_TILE'", null);
        int nextX = cursor.getCount() + 1;
        cursor.close();

        database.execSQL("INSERT INTO 'CACHED_TILE' ('X') VALUES('" + nextX + "')");
        database.close();

        if (tileCache.isOnSdCard()) {
            DocumentFile tileCacheRootDirectory =  mSdCardTileCacheAccess.getOrCreateTileCacheRootDirectory();
            File tileCacheDirectory =  new File(mSdCardTileCacheAccess.getOrCreateDirectory(mSdCardAccess.getSdCardAppDirectory(),
                    TileCacheManager.TILE_CACHE_ROOT_FOLDER_NAME), tileCache.getName());
            mSdCardAccess.moveDirectory(tileCacheDirectory, tileCacheRootDirectory);
        }
    }
}
