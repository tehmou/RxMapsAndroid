package com.tehmou.rxmaps.pojo;

/**
 * Created by ttuo on 28/08/14.
 */
public class MapTileDrawable extends MapTile {
    final double screenX;
    final double screenY;

    public MapTileDrawable(int zoom, int x, int y, double screenX, double screenY) {
        super(zoom, x, y);
        this.screenX = screenX;
        this.screenY = screenY;
    }

    public double getScreenX() {
        return screenX;
    }

    public double getScreenY() {
        return screenY;
    }
}
