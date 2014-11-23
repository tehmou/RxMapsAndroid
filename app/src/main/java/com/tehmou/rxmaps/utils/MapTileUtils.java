package com.tehmou.rxmaps.utils;

import com.tehmou.rxmaps.network.MapNetworkAdapter;
import com.tehmou.rxmaps.pojo.MapTile;
import com.tehmou.rxmaps.pojo.MapTileBitmap;
import com.tehmou.rxmaps.pojo.MapTileDrawable;
import com.tehmou.rxmaps.provider.RxMapsContentProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;

/**
 * Created by ttuo on 28/08/14.
 */
public class MapTileUtils {
    private static final String TAG = MapTileUtils.class.getCanonicalName();

    static public PointD calculateOffset(final CoordinateProjection coordinateProjection,
                                         final Integer zoomLevel,
                                         final PointD viewSize,
                                         final LatLng center) {
        final double mapPxSize = coordinateProjection.pxSize(zoomLevel);
        final PointD centerPx = coordinateProjection
                .fromLatLngToPoint(center.getLat(), center.getLng(), zoomLevel);
        final double offsetX2 = centerPx.x - (mapPxSize / 2.0);
        final double offsetY2 = centerPx.y - (mapPxSize / 2.0);
        final double centerOffsetX = (viewSize.x - mapPxSize) / 2.0;
        final double centerOffsetY = (viewSize.y - mapPxSize) / 2.0;
        final double offsetX = centerOffsetX - offsetX2;
        final double offsetY = centerOffsetY - offsetY2;
        Log.d(TAG, "offsetPx(" + offsetX + ", " + offsetY + ")");
        return new PointD(offsetX, offsetY);
    }

    static public Func1<MapState, Collection<MapTileDrawable>> calculateMapTiles(final double tileSizePx) {
        return mapState -> calculateMapTiles(tileSizePx, mapState.zoomLevel, mapState.viewSize, mapState.offset);
    }

    static private Collection<MapTileDrawable> calculateMapTiles(final double tileSizePx,
                                                                 final Integer zoomLevel,
                                                                 final PointD viewSize,
                                                                 final PointD offset) {
        final int firstTileX = (int) Math.floor(-offset.x / tileSizePx);
        final int firstTileY = (int) Math.floor(-offset.y / tileSizePx);
        final int numX = (int) Math.ceil(viewSize.x / tileSizePx);
        final int numY = (int) Math.ceil(viewSize.y / tileSizePx);

        final int left = Math.max(0, firstTileX);
        final int right = Math.min(1 << zoomLevel, firstTileX + numX);
        final int top = Math.max(0, firstTileY);
        final int bottom = Math.min(1 << zoomLevel, firstTileY + numY);

        final List<MapTileDrawable> mapTileList = new ArrayList<MapTileDrawable>();
        for (int i = left; i < right; i++) {
            for (int n = top; n < bottom; n++) {
                final MapTileDrawable mapTile = new MapTileDrawable(
                        zoomLevel, i, n, tileSizePx,
                        i*tileSizePx + offset.x,
                        n*tileSizePx + offset.y);
                mapTileList.add(mapTile);
            }
        }
        return mapTileList;
    }

    static public Func3<Integer, PointD, LatLng, MapState> combineToMapState(
            final CoordinateProjection coordinateProjection) {
        return (zoomLevel, viewSize, center) -> {
            final PointD offset = calculateOffset(
                    coordinateProjection, zoomLevel, viewSize, center);
            return new MapState(offset, viewSize, zoomLevel);
        };
    }

    static public Uri createUri(final MapTile mapTile) {
        Uri uri = RxMapsContentProvider.MAP_TILE_BITMAPS_CONTENT_URI;
        uri = Uri.withAppendedPath(uri, String.valueOf(mapTile.getZoom()));
        uri = Uri.withAppendedPath(uri, String.valueOf(mapTile.getX()));
        uri = Uri.withAppendedPath(uri, String.valueOf(mapTile.getY()));
        return uri;
    }

    static private byte[] bitmapToByte(final Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    static private Bitmap byteToBitmap(final byte[] bitmapBlob) {
        return BitmapFactory.decodeByteArray(bitmapBlob, 0, bitmapBlob.length);
    }

    static public Func1<MapTile, Observable<MapTileBitmap>> loadMapTile(
            final MapNetworkAdapter mapNetworkAdapter) {
        return mapTile -> mapNetworkAdapter.getMapTile(
                mapTile.getZoom(), mapTile.getX(), mapTile.getY())
                .map(bitmap -> new MapTileBitmap(mapTile, bitmap))
                .onErrorResumeNext(throwable -> {
                    Log.e(TAG, "Error loading tile (" + mapTile + ")", throwable);
                    throwable.printStackTrace();
                    return Observable.from(new MapTileBitmap(mapTile, null));
                });
    }

    static public <T> Action1<T> logOnNext(final String tag) {
        return value -> Log.d(TAG, tag + ": " + value);
    }

    static final public Func1<Collection<MapTileDrawable>, Observable<MapTileDrawable>> expandCollection =
            Observable::from;
}
