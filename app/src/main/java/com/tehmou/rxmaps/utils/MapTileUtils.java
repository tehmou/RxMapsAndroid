package com.tehmou.rxmaps.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.tehmou.rxmaps.network.MapNetworkAdapter;
import com.tehmou.rxmaps.pojo.MapTile;

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

    static public Func1<MapState, Collection<MapTile>> calculateMapTiles(final double tileSizePx) {
        return new Func1<MapState, Collection<MapTile>>() {
            @Override
            public Collection<MapTile> call(MapState mapState) {
                return calculateMapTiles(tileSizePx, mapState.zoomLevel, mapState.viewSize, mapState.offset);
            }
        };
    }

    static private Collection<MapTile> calculateMapTiles(final double tileSizePx,
                                                        final Integer zoomLevel,
                                                        final PointD viewSize,
                                                        final PointD offset) {
        final int firstTileX = (int) Math.floor(-offset.x / tileSizePx);
        final int firstTileY = (int) Math.floor(-offset.y / tileSizePx);
        final int numX = (int) Math.ceil(viewSize.x / tileSizePx);
        final int numY = (int) Math.ceil(viewSize.y / tileSizePx);

        final List<MapTile> mapTileList = new ArrayList<MapTile>();
        for (int i = firstTileX; i <= firstTileX + numX; i++) {
            for (int n = firstTileY; n <= firstTileY + numY; n++) {
                final MapTile mapTile = new MapTile(zoomLevel, i, n, null);
                mapTileList.add(mapTile);
            }
        }
        return mapTileList;
    }

    static public Func3<Integer, PointD, LatLng, MapState> combineToMapState(
            final CoordinateProjection coordinateProjection) {
        return new Func3<Integer, PointD, LatLng, MapState>() {
            @Override
            public MapState call(Integer zoomLevel,
                                              PointD viewSize,
                                              LatLng center) {
                final PointD offset = calculateOffset(
                        coordinateProjection, zoomLevel, viewSize, center);
                return new MapState(offset, viewSize, zoomLevel);
            }
        };
    }

    static public Func1<MapTile, Observable<MapTile>> loadMapTile(final MapNetworkAdapter mapNetworkAdapter) {
        return new Func1<MapTile, Observable<MapTile>>() {
            @Override
            public Observable<MapTile> call(final MapTile mapTile) {
                return mapNetworkAdapter.getMapTile(
                        mapTile.getZoom(), mapTile.getX(), mapTile.getY())
                        .map(new Func1<Bitmap, MapTile>() {
                            @Override
                            public MapTile call(Bitmap bitmap) {
                                return new MapTile(mapTile, bitmap);
                            }
                        })
                        .onErrorResumeNext(new Func1<Throwable, Observable<? extends MapTile>>() {
                            @Override
                            public Observable<? extends MapTile> call(Throwable throwable) {
                                return Observable.from(new MapTile(mapTile, null));
                            }
                        });
            }
        };
    }

    static public <T> Action1<T> logOnNext(final String tag) {
        return new Action1<T>() {
            @Override
            public void call(T value) {
                Log.d(TAG, tag + ": " + value);
            }
        };
    }

    static final public Func1<Collection<MapTile>, Observable<MapTile>> expandCollection =
            new Func1<Collection<MapTile>, Observable<MapTile>>() {
                @Override
                public Observable<MapTile> call(Collection<MapTile> mapTiles) {
                    return Observable.from(mapTiles);
                }
            };
}
