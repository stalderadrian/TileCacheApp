package com.example.stalde_a.tilecacheapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InternalStorageTileCacheAccess {

    private Context mContext;
    private TileCacheSQLiteHelper mTileCacheSQLiteHelper;

    public InternalStorageTileCacheAccess(Context context, TileCacheSQLiteHelper tileCacheSQLiteHelper) {
        mContext = context;
        mTileCacheSQLiteHelper = tileCacheSQLiteHelper;
    }

    public TileCache create(String name) throws Exception {
        DocumentFile tileCacheRootDirectory = getOrCreateTileCacheRootDirectory();
        DocumentFile tileCacheDirectory = tileCacheRootDirectory.findFile(name);
        if (tileCacheDirectory != null && tileCacheDirectory.exists()) {
            throw new Exception("Directory with the same name already exists");
        }
        tileCacheDirectory = tileCacheRootDirectory.createDirectory(name);
        if (tileCacheDirectory == null || !tileCacheDirectory.exists()) {
            throw new Exception("Could not create directory '" + name + "'");
        }
        return new TileCache(name, false);
    }

    @NonNull
    private DocumentFile getOrCreateTileCacheRootDirectory() throws Exception {
        File rootDirectory = getRootDirectoryAsFile();
        DocumentFile rootDocumentDirectory = DocumentFile.fromFile(
                rootDirectory);
        DocumentFile directory = StorageAccessFrameworkUtils.findDirectory(
                rootDocumentDirectory, TileCacheManager.TILE_CACHE_ROOT_FOLDER_NAME);
        if (directory != null) {
            return directory;
        } else {
            return StorageAccessFrameworkUtils.createDirectory(
                    rootDocumentDirectory, TileCacheManager.TILE_CACHE_ROOT_FOLDER_NAME);
        }
    }

    private File getRootDirectoryAsFile() throws Exception {
        File rootDirectory = mContext.getExternalFilesDir(null); // TODO: Nicht das richtige Verzeichnis
        if (rootDirectory == null || !rootDirectory.exists()) {
            throw new Exception("Internal storage root directory not found");
        }
        return rootDirectory;
    }

    public List<TileCache> getTileCaches() throws Exception {
        List<TileCache> tileCaches = new ArrayList<>();
        DocumentFile tileCacheRootDirectory = getOrCreateTileCacheRootDirectory();
        if (!tileCacheRootDirectory.exists()) {
            throw new Exception("Tile cache root directory does not exist");
        }
        for (DocumentFile tileCache : tileCacheRootDirectory.listFiles()) {
            if (tileCache.isDirectory()) {
                tileCaches.add(new TileCache(tileCache.getName(), false));
            }
        }
        return tileCaches;
    }

    public File getOrCreateTileCacheRootDirectoryAsFile() throws Exception {
        File rootDirectory = getRootDirectoryAsFile();
        File tileCacheRootDirectory = new File(rootDirectory, TileCacheManager.TILE_CACHE_ROOT_FOLDER_NAME);
        if (!tileCacheRootDirectory.exists() && !tileCacheRootDirectory.mkdirs()) {
            throw new Exception("Could not create tile cache root directory '" + tileCacheRootDirectory.getAbsolutePath() + "'");
        }
        return tileCacheRootDirectory;
    }

    public void delete(TileCache tileCache) throws Exception {
        DocumentFile tileCacheDirectory = StorageAccessFrameworkUtils.findDirectory(getOrCreateTileCacheRootDirectory(),
                tileCache.getName());
        if (tileCacheDirectory != null && tileCacheDirectory.exists()) {
            tileCacheDirectory.delete();
        }
    }

    public SQLiteDatabase getDatabase(TileCache tileCache) throws Exception {
        return mTileCacheSQLiteHelper.getWritableDatabase(getTileCacheDirectoryAsFile(tileCache));
    }

    private File getTileCacheDirectoryAsFile(TileCache tileCache) throws Exception {
        File tileCacheRootDirectory = getOrCreateTileCacheRootDirectoryAsFile();
        if (tileCacheRootDirectory == null || !tileCacheRootDirectory.exists()) {
            throw new Exception("Tile cache root directory '" + tileCacheRootDirectory.getAbsolutePath() + "' not found");
        }
        File tileCacheDirectory = new File(tileCacheRootDirectory, tileCache.getName());
        if (!tileCacheDirectory.exists()) {
            throw new Exception("Tile cache directory '" + tileCacheDirectory.getAbsolutePath() + "' not found");
        }
        return tileCacheDirectory;
    }
}
