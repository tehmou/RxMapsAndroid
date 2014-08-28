package com.tehmou.rxmaps.utils;

/**
 * Created by ttuo on 27/08/14.
 */
public class LatLng {
    final private double lat;
    final private double lng;

    public LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
