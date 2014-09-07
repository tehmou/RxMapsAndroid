package com.tehmou.rxmaps.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tehmou.rxmaps.pojo.MapTile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import rx.functions.Action1;

/**
 * Created by ttuo on 07/09/14.
 */
public class MapTileFileUtils {
    private MapTileFileUtils() { }

    static public String getFilename(final MapTile mapTile) {
        final int zoom = mapTile.getZoom();
        final int x = mapTile.getX();
        final int y = mapTile.getY();
        return "/data/data/com.tehmou.rxmaps/" + zoom + "_" + x + "_" + y + ".png";
    }

    static public Bitmap readFromDisk(final String filename) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(filename);
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

    static public void writeOnDisk(final String filename, final Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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
}
