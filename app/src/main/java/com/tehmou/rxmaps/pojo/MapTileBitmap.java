package com.tehmou.rxmaps.pojo;

import android.graphics.Bitmap;

/**
 * Created by ttuo on 28/08/14.
 */
public class MapTileBitmap {
    final private MapTile mapTile;
    final private Bitmap bitmap;

    public MapTileBitmap(MapTile mapTile, Bitmap bitmap) {
        this.mapTile = mapTile;
        this.bitmap = bitmap;
    }

    public MapTile getMapTile() {
        return mapTile;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
