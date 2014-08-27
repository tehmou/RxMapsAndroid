package com.tehmou.rxmaps.pojo;

/**
 * Created by ttuo on 27/08/14.
 */
public class MapTile {
    final private int zoom;
    final private int x;
    final private int y;
    final private int screenX;
    final private int screenY;

    public MapTile(int zoom, int x, int y, int screenX, int screenY) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
        this.screenX = screenX;
        this.screenY = screenY;
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

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }
}
