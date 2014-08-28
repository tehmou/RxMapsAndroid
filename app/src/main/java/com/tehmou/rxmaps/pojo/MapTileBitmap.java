package com.tehmou.rxmaps.pojo;

import android.graphics.Bitmap;

/**
 * Created by ttuo on 28/08/14.
 */
public class MapTileBitmap {
    final private int tileHashCode;
    final private Bitmap bitmap;

    public MapTileBitmap(int tileHashCode, Bitmap bitmap) {
        this.tileHashCode = tileHashCode;
        this.bitmap = bitmap;
    }

    public int getTileHashCode() {
        return tileHashCode;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
