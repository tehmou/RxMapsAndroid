package com.tehmou.rxmaps.network;

import android.graphics.Bitmap;
import android.util.Log;

import com.tehmou.rxmaps.Configuration;

import rx.Observable;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapNetworkAdapterSimple implements MapNetworkAdapter {
    private static final String TAG = MapNetworkAdapterSimple.class.getCanonicalName();
    final private NetworkClient networkClient;
    final private String urlFormat;

    public MapNetworkAdapterSimple(
            final NetworkClient networkClient,
            final String urlFormat) {
        this.networkClient = networkClient;
        this.urlFormat = urlFormat;
    }

    public Observable<Bitmap> getMapTile(int zoom, int x, int y) {
        Log.d(TAG, "getMapTile(" + zoom + ", " + x + ", " + y + ")");
        final String url = String.format(urlFormat, zoom, x, y);
        return networkClient.loadBitmap(url);
    }

    @Override
    public int getTileSizePx() {
        return 128;
    }
}
