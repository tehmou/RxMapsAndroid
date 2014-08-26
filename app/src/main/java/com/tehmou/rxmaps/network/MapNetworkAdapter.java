package com.tehmou.rxmaps.network;

import android.graphics.Bitmap;
import android.util.Log;

import com.tehmou.rxmaps.Configuration;

import rx.Observable;

/**
 * Created by ttuo on 26/08/14.
 */
public interface MapNetworkAdapter {
    Observable<Bitmap> getMapTile(int zoom, int x, int y);
}
