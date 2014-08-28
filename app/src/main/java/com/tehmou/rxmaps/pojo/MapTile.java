package com.tehmou.rxmaps.pojo;

import android.graphics.Bitmap;

/**
 * Created by ttuo on 27/08/14.
 */
public class MapTile {
    final private int zoom;
    final private int x;
    final private int y;
    private Bitmap bitmap;

    public MapTile(int zoom, int x, int y, Bitmap bitmap) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
    }

    public MapTile(MapTile mapTile, Bitmap bitmap) {
        this.zoom = mapTile.zoom;
        this.x = mapTile.x;
        this.y = mapTile.y;
        this.bitmap = bitmap;
    }

    public int getZoom() {
        return zoom;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
