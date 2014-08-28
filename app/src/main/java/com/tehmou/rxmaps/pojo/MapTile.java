package com.tehmou.rxmaps.pojo;

/**
 * Created by ttuo on 27/08/14.
 */
public class MapTile {
    final private int zoom;
    final private int x;
    final private int y;
    final private double screenX;
    final private double screenY;

    public MapTile(int zoom, int x, int y, double screenX, double screenY) {
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

    public double getScreenX() {
        return screenX;
    }

    public double getScreenY() {
        return screenY;
    }
}
