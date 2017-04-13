package com.example.stalde_a.tilecacheapp;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SdCardTileCacheAccess {

    public static final String TAG = SdCardTileCacheAccess.class.getSimpleName();

    private SdCardAccess mSdCardAccess;
    private TileCacheSQLiteHelper mTileCacheSQLiteHelper;

    public SdCardTileCacheAccess(SdCardAccess sdCardAccess, TileCacheSQLiteHelper tileCacheSQLiteHelper) {
        mSdCardAccess = sdCardAccess;
        mTileCacheSQLiteHelper = tileCacheSQLiteHelper;
    }

    public TileCache create(String name) throws Exception {
        DocumentFile tileCacheDirectory = mSdCardAccess.getOrCreateDirectory(
                getOrCreateTileCacheRootDirectory(), name);
        if (tileCacheDirectory == null || !tileCacheDirectory.exists()) {
            throw new Exception("Could not create directory '" + name + "' on SD card");
        }
        return new TileCache(name, true);
    }

    private File getTileCacheDirectoryAsFile(TileCache tileCache) throws Exception {
        File tileCacheRootDirectory = getOrCreateTileCacheRootDirectoryAsFile();
        if (tileCacheRootDirectory == null || !tileCacheRootDirectory.exists()) {
            throw new Exception("Tile cache root directory '" +
                    tileCacheRootDirectory.getAbsolutePath() + "' not found");
        }
        File tileCacheDirectory = new File(tileCacheRootDirectory, tileCache.getName());
        if (!tileCacheDirectory.exists()) {
            throw new Exception("Tile cache directory '" + tileCacheDirectory.getAbsolutePath() +
                    "' not found");
        }
        return tileCacheDirectory;
    }

    private DocumentFile getOrCreateTileCacheRootDirectory() throws Exception {
        DocumentFile sdCardRootDirectory = mSdCardAccess.getSdCardRootDirectory();
        if (sdCardRootDirectory == null || !sdCardRootDirectory.exists()) {
            throw new Exception("SD card root directory not found");
        }
        return mSdCardAccess.getOrCreateDirectory(
                sdCardRootDirectory, TileCacheManager.TILE_CACHE_ROOT_FOLDER_NAME);
    }

    public File getOrCreateTileCacheRootDirectoryAsFile() throws Exception {
        File sdCardRootDirectory = mSdCardAccess.getSdCardRootDirectoryAsFile();
        File tileCacheRootDirectory = new File(sdCardRootDirectory,
                TileCacheManager.TILE_CACHE_ROOT_FOLDER_NAME);
        if (!tileCacheRootDirectory.exists() && !tileCacheRootDirectory.mkdirs()) {
            throw new Exception("Could not create tile cache root directory '" +
                    tileCacheRootDirectory.getAbsolutePath() + "'");
        }
        return tileCacheRootDirectory;
    }

    public List<TileCache> getTileCaches() throws Exception {
        List<TileCache> tileCaches = new ArrayList<>();
        DocumentFile sdCardRootDirectory = mSdCardAccess.getSdCardRootDirectory();
        if (sdCardRootDirectory != null && sdCardRootDirectory.exists()) {
            DocumentFile tileCacheRootDirectory = getOrCreateTileCacheRootDirectory();
            if (tileCacheRootDirectory == null || !tileCacheRootDirectory.exists()) {
                throw new Exception("Tile cache root directory does not exist");
            }
            for (DocumentFile tileCache : tileCacheRootDirectory.listFiles()) {
                if (tileCache.isDirectory()) {
                    tileCaches.add(new TileCache(tileCache.getName(), true));
                }
            }
        }
        return tileCaches;
    }

    public void delete(TileCache tileCache) throws Exception {
        DocumentFile tileCacheDirectory = StorageAccessFrameworkUtils.findDirectory(getOrCreateTileCacheRootDirectory(),
                tileCache.getName());
        if (tileCacheDirectory != null && tileCacheDirectory.exists()) {
            tileCacheDirectory.delete();
        }
    }

    public SQLiteDatabase getDatabase(TileCache tileCache) throws Exception {
        File tileCacheDirectory = getTileCacheDirectoryAsFile(tileCache);
        File databaseFile = new File(tileCacheDirectory, TileCacheSQLiteHelper.DATABASE_NAME);
        if (!databaseFile.exists()) {
            File tempTileCacheRootDirectory = new File(mSdCardAccess.getSdCardAppDirectory(),
                    TileCacheManager.TILE_CACHE_ROOT_FOLDER_NAME);
            if (!tempTileCacheRootDirectory.exists() && !tempTileCacheRootDirectory.mkdirs()) {
                throw new Exception("Could not create tile cache directory '" +
                        tempTileCacheRootDirectory.getAbsolutePath() + "'");
            }
            File tempTileCacheDirectory = new File(tempTileCacheRootDirectory, tileCache.getName());
            if (!tempTileCacheDirectory.exists() && !tempTileCacheDirectory.mkdirs()) {
                throw new Exception("Could not create tile cache directory '" +
                        tempTileCacheDirectory.getAbsolutePath() + "'");
            }

            SQLiteDatabase database = mTileCacheSQLiteHelper.getWritableDatabase(
                    tempTileCacheDirectory);
            database.close();

            mSdCardAccess.moveDirectory(tempTileCacheDirectory, getOrCreateTileCacheRootDirectory());

            // TODO: Use FileUtils
            if (!deleteRecursive(tempTileCacheDirectory)) {
                Log.w(TAG, "Could not delete directoy '" + tempTileCacheDirectory.getAbsolutePath() + "'");
            }
        }
        return mTileCacheSQLiteHelper.getReadableDatabase(tileCacheDirectory);
    }

    private boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File file : path.listFiles()){
                ret = ret && deleteRecursive(file);
            }
        }
        return ret && path.delete();
    }
}
