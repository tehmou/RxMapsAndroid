package com.tehmou.rxmaps.utils;

/**
 * Created by ttuo on 27/08/14.
 */
public class PointD {
    final public double x;
    final public double y;

    public PointD(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        int result = Double.valueOf(x).hashCode();
        result += 31 * result + Double.valueOf(y).hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PointD && o.hashCode() == hashCode();
    }

    @Override
    public String toString() {
        return "PointD(" + x + ", " + y + ")";
    }
}
