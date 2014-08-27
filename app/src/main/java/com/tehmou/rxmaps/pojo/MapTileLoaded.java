package com.tehmou.rxmaps.pojo;

import android.graphics.Bitmap;

/**
 * Created by ttuo on 27/08/14.
 */
public class MapTileLoaded extends MapTile {
    private Bitmap bitmap;

    public MapTileLoaded(MapTile mapTile,
                         Bitmap bitmap) {
        super(mapTile.getZoom(),
                mapTile.getX(),
                mapTile.getY(),
                mapTile.getScreenX(),
                mapTile.getScreenY());
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
