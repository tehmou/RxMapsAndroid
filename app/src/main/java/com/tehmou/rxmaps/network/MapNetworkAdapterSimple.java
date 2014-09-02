package com.tehmou.rxmaps.network;

import android.graphics.Bitmap;
import android.util.Log;

import com.tehmou.rxmaps.Configuration;

import java.io.FileOutputStream;
import java.io.IOException;

import rx.Observable;
import rx.functions.Action1;

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

    public Observable<Bitmap> getMapTile(final int zoom, final int x, final int y) {
        Log.d(TAG, "getMapTile(" + zoom + ", " + x + ", " + y + ")");
        final String url = String.format(urlFormat, zoom, x, y);
        return networkClient
                .loadBitmap(url);
                //.doOnNext(writeOnDisk(zoom, x, y));
    }

    static private Action1<Bitmap> writeOnDisk(final int zoom, final int x, final int y) {
        return new Action1<Bitmap>() {
            @Override
            public void call(Bitmap bitmap) {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream("/data/data/com.tehmou.rxmaps/" + zoom + "_" + x + "_" + y + ".png");
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    public int getTileSizePx() {
        return 256;
    }
}
