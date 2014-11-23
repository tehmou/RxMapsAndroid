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

    @Override
    public int hashCode() {
        int result = offset != null ? offset.hashCode() : 0;
        result += 31 * result + (viewSize != null ? viewSize.hashCode() : 0);
        result += 31 * result + zoomLevel;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MapState && o.hashCode() == hashCode();
    }

    @Override
    public String toString() {
        return "MapState(offset=" + offset + ", viewSize=" + viewSize + ", zoomLevel=" + zoomLevel + ")";
    }
}
