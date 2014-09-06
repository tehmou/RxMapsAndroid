package com.tehmou.rxmaps.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;

import com.tehmou.rxmaps.network.MapNetworkAdapter;
import com.tehmou.rxmaps.pojo.MapTileBitmap;
import com.tehmou.rxmaps.pojo.MapTileDrawable;
import com.tehmou.rxmaps.provider.RxMapsContentProvider;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by ttuo on 03/09/14.
 */
public class TileBitmapLoader implements Func1<Collection<MapTileDrawable>, Observable<Map<Integer, Bitmap>>> {
    final private ContentResolver contentResolver;
    final private MapNetworkAdapter mapNetworkAdapter;
    final private Map<Integer, Bitmap> loadedTileBitmaps = new ConcurrentHashMap<Integer, Bitmap>();

    public TileBitmapLoader(final ContentResolver contentResolver,
                            final MapNetworkAdapter mapNetworkAdapter) {
        this.contentResolver = contentResolver;
        this.mapNetworkAdapter = mapNetworkAdapter;
    }

    @Override
    public Observable<Map<Integer, Bitmap>> call(Collection<MapTileDrawable> mapTileDrawables) {
        return Observable.from(mapTileDrawables)
                .filter(new Func1<MapTileDrawable, Boolean>() {
                    @Override
                    public Boolean call(final MapTileDrawable mapTileDrawable) {
                        return !loadedTileBitmaps.containsKey(mapTileDrawable.tileHashCode());
                    }
                })
                .flatMap(MapTileUtils.loadMapTile(contentResolver, mapNetworkAdapter))
                .map(new Func1<MapTileBitmap, Map<Integer, Bitmap>>() {
                    @Override
                    public Map<Integer, Bitmap> call(final MapTileBitmap mapTileBitmap) {
                        if (mapTileBitmap != null && mapTileBitmap.getBitmap() != null) {
                            loadedTileBitmaps.put(mapTileBitmap.getTileHashCode(),
                                    mapTileBitmap.getBitmap());
                        }
                        return loadedTileBitmaps;
                    }
                });
    }
}
