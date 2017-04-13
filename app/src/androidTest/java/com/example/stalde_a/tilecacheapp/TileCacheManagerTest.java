package com.example.stalde_a.tilecacheapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by Adrian Stalder on 12.04.2017.
 */
@RunWith(AndroidJUnit4.class)
public class TileCacheManagerTest {

    private TileCacheManager mTileCacheManager;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getTargetContext();
        mTileCacheManager = new TileCacheManager(context, new SdCardAccess(context));
    }

    @Test
    public void creates_tile_cache_directory_on_sd_card() throws Exception {
        //Arrange
        String tileCacheName = "Test";
        Boolean saveOnSdCard = true;

        //Act
        mTileCacheManager.create(tileCacheName, saveOnSdCard);

        //Assert
        assertEquals(true, new File("/storage/189D-DBB7/TileCacheApp/" + tileCacheName + "/test.cache").exists());
    }

    @After
    public void tearDown() {

    }
}
