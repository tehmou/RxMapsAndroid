package com.tehmou.rxmaps.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tehmou.rxmaps.Configuration;

import java.io.FileInputStream;
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

        return Observable.from(readFromDisk(zoom, x, y));
        //return networkClient
        //        .loadBitmap(url);
        //        .doOnNext(writeOnDisk(zoom, x, y));
    }

    static private String getFilename(final int zoom, final int x, final int y) {
        return "/data/data/com.tehmou.rxmaps/" + zoom + "_" + x + "_" + y + ".png";
    }

    static private Bitmap readFromDisk(final int zoom, final int x, final int y) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(getFilename(zoom, x, y));
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static private Action1<Bitmap> writeOnDisk(final int zoom, final int x, final int y) {
        return new Action1<Bitmap>() {
            @Override
            public void call(Bitmap bitmap) {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(getFilename(zoom, x, y));
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
