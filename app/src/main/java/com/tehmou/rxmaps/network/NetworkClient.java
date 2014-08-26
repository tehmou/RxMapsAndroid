package com.tehmou.rxmaps.network;

import android.graphics.Bitmap;

import rx.Observable;

/**
 * Created by ttuo on 26/08/14.
 */
public interface NetworkClient {
    Observable<String> loadString(final String url);
    Observable<Bitmap> loadBitmap(final String url);
}