package com.tehmou.rxmaps.utils;

/**
 * Created by ttuo on 28/08/14.
 */
public class MapState {
    final public PointD offset;
    final public PointD viewSize;
    final public int zoomLevel;

    public MapState(PointD offset, PointD viewSize, int zoomLevel) {
        this.offset = offset;
        this.viewSize = viewSize;
        this.zoomLevel = zoomLevel;
    }
}
