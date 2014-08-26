package com.tehmou.rxmaps;

import android.graphics.Bitmap;

import com.tehmou.rxmaps.network.MapNetworkAdapter;

import rx.Observable;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapViewModel {
    final private MapNetworkAdapter mapNetworkAdapter;

    public MapViewModel(final MapNetworkAdapter mapNetworkAdapter) {
        this.mapNetworkAdapter = mapNetworkAdapter;
    }

    public void subscribe() {

    }

    public void unsubscribe() {

    }

    public Observable<Bitmap> getBitmap() {
        return mapNetworkAdapter.getMapTile(0, 0, 0);
    }
}
