package com.example.stalde_a.tilecacheapp;

public class TileCache {

    private String mName;
    private boolean mIsOnSdCard;

    public TileCache(String name, boolean isOnSdCard) {
        mName = name;
        mIsOnSdCard = isOnSdCard;
    }

    public String getName() {
        return mName;
    }

    public boolean isOnSdCard() {
        return mIsOnSdCard;
    }

    @Override
    public String toString() {
        if (!mIsOnSdCard) {
            return this.mName;
        } else {
            return this.mName + " --> SD";
        }
    }
}
