package com.example.stalde_a.tilecacheapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TileCacheManager {

    public static final String TILE_CACHE_ROOT_FOLDER_NAME = "TileCacheApp";

    private final InternalStorageTileCacheAccess mInternalStorageTileCacheAccess;
    private final SdCardTileCacheAccess mSdCardTileCacheAccess;

    public TileCacheManager(Context context, SdCardAccess sdCardAccess) {
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

    public SQLiteDatabase getDatabase(TileCache tileCache) throws Exception {
        if (tileCache.isOnSdCard()) {
            return mSdCardTileCacheAccess.getDatabase(tileCache);
        } else {
            return mInternalStorageTileCacheAccess.getDatabase(tileCache);
        }
    }
}
