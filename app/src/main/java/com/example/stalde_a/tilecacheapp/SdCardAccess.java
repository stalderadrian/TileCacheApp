package com.example.stalde_a.tilecacheapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.support.v4.provider.DocumentFile;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class SdCardAccess {

    private Context mContext;

    public SdCardAccess(Context context) {
        this.mContext = context;
    }

    public File getSdCardAppDirectory() throws Exception {
        File[] externalFilesDirs = mContext.getExternalFilesDirs(null);
        if (externalFilesDirs.length <= 1) {
            throw new Exception("No SD card found");
        }
        File sdCardAppDirectory = externalFilesDirs[1];
        if (sdCardAppDirectory == null || !sdCardAppDirectory.exists()) {
            throw new Exception("SD card app directory not found");
        }
        return sdCardAppDirectory;
    }

    public DocumentFile getSdCardRootDirectory() {
        Uri sdCardTreeUri = getSdCardTreeUri();
        if (sdCardTreeUri != null) {
            return DocumentFile.fromTreeUri(this.mContext, sdCardTreeUri);
        } else {
            return null;
        }
    }

    public File getSdCardRootDirectoryAsFile() throws Exception {
        File sdCardRootDirectory = getSdCardAppDirectory().getParentFile().getParentFile().getParentFile().getParentFile(); // TODO: Bessere Methode
        if (sdCardRootDirectory == null || !sdCardRootDirectory.exists()) {
            throw new Exception("SD card root directory not found");
        }
        return sdCardRootDirectory;
    }

    public Uri getSdCardTreeUri() {
        SharedPreferences settings = mContext.getSharedPreferences("PREFS_NAME", 0);
        String sdCardTreeUriString = settings.getString("imageURI", null);
        if (sdCardTreeUriString == null) {
            return null;
        }
        return Uri.parse(sdCardTreeUriString);
    }

    public void setSdCardTreeUri(Uri sdCardTreeUri) {
        SharedPreferences settings = mContext.getSharedPreferences("PREFS_NAME", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("imageURI", sdCardTreeUri.toString());
        editor.apply();
    }

    public DocumentFile getOrCreateDirectory(DocumentFile parentDirectory, String directoryName)
            throws Exception {
        DocumentFile directory = StorageAccessFrameworkUtils.findDirectory(parentDirectory,
                directoryName);
        if (directory == null) {
            if (!hasWritePermission()) {
                throw new Exception(
                        "Missing permisson to create directory '" + directoryName + "'");
            }
            return StorageAccessFrameworkUtils.createDirectory(parentDirectory, directoryName);

        } else {
            return directory;
        }
    }

    public boolean hasWritePermission() throws Exception {
        Uri sdCardTreeUri = getSdCardTreeUri();
        if (sdCardTreeUri == null) {
            return false;
        }

        int uriPermission = mContext.checkUriPermission(sdCardTreeUri,
                Binder.getCallingPid(), Binder.getCallingUid(),
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return uriPermission != PackageManager.PERMISSION_DENIED;
    }

    public void moveDirectory(File sourceDir, DocumentFile targetParentDir) throws Exception {
        DocumentFile targetDir = targetParentDir.findFile(sourceDir.getName());
        if (targetDir == null || !targetDir.exists()) {
            targetDir = targetParentDir.createDirectory(sourceDir.getName());
            if (targetDir == null || !targetDir.exists()) {
                throw new Exception("Could not create directory '" + sourceDir.getName() + "'");
            }
        }
        for (File file : sourceDir.listFiles()) {
            if (file.isDirectory()) {
                moveDirectory(file, targetDir);
            } else {
                moveFile(file, targetDir);
            }
        }
    }

    public void moveFile(File sourceFile, DocumentFile targetParentDir) throws Exception {
        InputStream in;
        OutputStream out;

        String mimeType = getMimeFrom(sourceFile.toURI().toString());
        DocumentFile targetFile = targetParentDir.createFile(mimeType, sourceFile.getName());
        out = mContext.getContentResolver().openOutputStream(targetFile.getUri());
        in = new FileInputStream(sourceFile);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        out.close();
    }

    private static String getMimeFrom(String uri) {
        String type = null;
        String extention = MimeTypeMap.getFileExtensionFromUrl(uri);
        if (extention != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extention);
        }
        return type;
    }
}
