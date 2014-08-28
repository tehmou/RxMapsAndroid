package com.tehmou.rxmaps.pojo;

import android.graphics.Bitmap;

/**
 * Created by ttuo on 27/08/14.
 */
public class MapTile {
    final private int zoom;
    final private int x;
    final private int y;

    public MapTile(int zoom, int x, int y) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
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

    public int tileHashCode() {
        int hash = zoom;
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        return hash;
    }
}
