package com.tehmou.rxmaps.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

import com.tehmou.rxmaps.data.MapTileFileUtils;
import com.tehmou.rxmaps.network.MapNetworkAdapter;
import com.tehmou.rxmaps.pojo.MapTile;
import com.tehmou.rxmaps.pojo.MapTileBitmap;
import com.tehmou.rxmaps.pojo.MapTileDrawable;
import com.tehmou.rxmaps.provider.MapTileBitmapsTable;
import com.tehmou.rxmaps.provider.RxMapsContentProvider;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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
                .flatMap(loadTileBitmap)
                .map(new Func1<MapTileBitmap, Map<Integer, Bitmap>>() {
                    @Override
                    public Map<Integer, Bitmap> call(final MapTileBitmap mapTileBitmap) {
                        if (mapTileBitmap != null && mapTileBitmap.getBitmap() != null) {
                            loadedTileBitmaps.put(mapTileBitmap.getMapTile().tileHashCode(),
                                    mapTileBitmap.getBitmap());
                        }
                        return loadedTileBitmaps;
                    }
                });
    }

    private MapTileBitmap getFromCache(final MapTile mapTile) {
        final Uri uri = MapTileUtils.createUri(mapTile);
        final Cursor cursor = contentResolver.query(uri, MapTileBitmapsTable.PROJECTION, null, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }
        cursor.moveToFirst();
        final String filename = cursor.getString(
                cursor.getColumnIndex(MapTileBitmapsTable.COLUMN_BITMAP_FILENAME));
        cursor.close();
        final Bitmap bitmap = MapTileFileUtils.readFromDisk(filename);
        return new MapTileBitmap(mapTile, bitmap);
    }

    private Observable<MapTileBitmap> getFromNetwork(final MapTile mapTile) {
        return MapTileUtils.loadMapTile(mapNetworkAdapter)
                .call(mapTile)
                .map(writeToContentProvider);
    }

    private Func1<MapTileBitmap, MapTileBitmap> writeToContentProvider =
            new Func1<MapTileBitmap, MapTileBitmap>() {
                @Override
                public MapTileBitmap call(MapTileBitmap mapTileBitmap) {
                    MapTileFileUtils.writeOnDisk(
                            MapTileFileUtils.getFilename(mapTileBitmap.getMapTile()), mapTileBitmap.getBitmap());

                    final Uri uri = MapTileUtils.createUri(mapTileBitmap.getMapTile());
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MapTileBitmapsTable.COLUMN_BITMAP_FILENAME,
                            MapTileFileUtils.getFilename(mapTileBitmap.getMapTile()));
                    contentResolver.insert(uri, contentValues);
                    return mapTileBitmap;
                }
            };

    private Func1<MapTileDrawable, Observable<MapTileBitmap>> loadTileBitmap =
            new Func1<MapTileDrawable, Observable<MapTileBitmap>>() {
                @Override
                public Observable<MapTileBitmap> call(final MapTileDrawable mapTileDrawable) {
                    return Observable.from((MapTile) mapTileDrawable)
                            .flatMap(new Func1<MapTile, Observable<MapTileBitmap>>() {
                                @Override
                                public Observable<MapTileBitmap> call(final MapTile mapTile) {
                                    final MapTileBitmap mapTileBitmap = getFromCache(mapTile);
                                    if (mapTileBitmap != null) {
                                        return Observable.from(mapTileBitmap);
                                    }
                                    return getFromNetwork(mapTile);
                                }
                            });
                }
            };
}
