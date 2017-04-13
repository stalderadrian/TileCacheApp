package com.example.stalde_a.tilecacheapp;

import android.support.v4.provider.DocumentFile;

public class StorageAccessFrameworkUtils {

    public static DocumentFile getDirectory(DocumentFile parentDirectory, String directoryName) throws Exception {
        DocumentFile directory = findDirectory(parentDirectory, directoryName);
        if (directory == null || !directory.exists()) {
            throw new Exception("Directory '" + directoryName + "' does not exist");
        }
        return directory;
    }

    public static DocumentFile findDirectory(DocumentFile parentDirectory, String directoryName) throws Exception {
        if (parentDirectory == null || !parentDirectory.exists()) {
            throw new Exception("Parent directory does not exist");
        }
        DocumentFile directory = parentDirectory.findFile(directoryName);
        if (directory != null && !directory.isDirectory()) {
            throw new Exception("Found file '" + directoryName + "' is no directory");
        }
        return directory;
    }

    public static DocumentFile findFile(DocumentFile parentDirectory, String fileName) throws Exception {
        if (parentDirectory == null || !parentDirectory.exists()) {
            throw new Exception("Parent directory does not exist");
        }
        DocumentFile file = parentDirectory.findFile(fileName);
        if (file != null && file.isDirectory()) {
            throw new Exception("Found file '" + fileName + "' is a directory");
        }
        return file;
    }

    public static DocumentFile createDirectory(DocumentFile parentDirectory, String directoryName) throws Exception {
        DocumentFile directory = parentDirectory.createDirectory(directoryName);
        if (directory == null || !directory.exists()) {
            throw new Exception("Could not create directory '" + directoryName + "'");
        }
        return directory;
    }
}
