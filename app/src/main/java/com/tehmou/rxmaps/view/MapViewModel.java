package com.tehmou.rxmaps.view;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.util.Pair;

import com.tehmou.rxmaps.network.MapNetworkAdapter;
import com.tehmou.rxmaps.utils.CoordinateProjection;
import com.tehmou.rxmaps.utils.LatLng;
import com.tehmou.rxmaps.pojo.MapTile;
import com.tehmou.rxmaps.pojo.ZoomLevel;
import com.tehmou.rxmaps.utils.PointD;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.functions.Func4;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapViewModel {
    private static final String TAG = MapViewModel.class.getCanonicalName();
    final private MapNetworkAdapter mapNetworkAdapter;
    final private Observable<Collection<MapTile>> mapTiles;
    final private Observable<MapTile> loadedMapTiles;
    final private Observable<PointD> offset;
    final private ZoomLevel zoomLevel;
    final private Subject<PointD, PointD> viewSize;
    final private Subject<LatLng, LatLng> centerCoord;
    final private CoordinateProjection coordinateProjection;

    public MapViewModel(final MapNetworkAdapter mapNetworkAdapter) {
        this.mapNetworkAdapter = mapNetworkAdapter;
        zoomLevel = new ZoomLevel(3);
        viewSize = PublishSubject.create();
        centerCoord = BehaviorSubject.create(new LatLng(51.507351, -0.127758));
        coordinateProjection = new CoordinateProjection(mapNetworkAdapter.getTileSizePx());

        final Subject<Collection<MapTile>, Collection<MapTile>> mapTilesSubject =
                BehaviorSubject.create();
        final Subject<MapTile, MapTile> loadedMapTilesSubject =
                PublishSubject.create();

        Observable<PointD> tempOffset = Observable.combineLatest(
                zoomLevel.getObservable()
                        .doOnNext(logOnNext("zoomLevel")),
                viewSize
                        .doOnNext(logOnNext("viewSize")),
                centerCoord,
                new Func3<Integer, PointD, LatLng, PointD>() {
                    @Override
                    public PointD call(Integer zoomLevel,
                                       PointD viewSize,
                                       LatLng center) {
                        return calculateOffset(zoomLevel, viewSize, center);
                    }
                })
                .cache();

        final Observable<Collection<MapTile>> mapTiles =
                Observable.combineLatest(
                        zoomLevel.getObservable()
                                .doOnNext(logOnNext("zoomLevel")),
                        viewSize
                                .doOnNext(logOnNext("viewSize")),
                        centerCoord,
                        new Func3<Integer, PointD, LatLng, Collection<MapTile>>() {
                            @Override
                            public Collection<MapTile> call(final Integer zoomLevel,
                                                            final PointD viewSize,
                                                            final LatLng center) {
                                final double tileSizePx = mapNetworkAdapter.getTileSizePx();
                                final PointD offset = calculateOffset(zoomLevel, viewSize, center);
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
                        })
                        .cache();

        mapTiles
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mapTilesSubject);

        mapTiles
                .flatMap(new Func1<Collection<MapTile>, Observable<MapTile>>() {
                    @Override
                    public Observable<MapTile> call(Collection<MapTile> mapTiles) {
                        return Observable.from(mapTiles);
                    }
                })
                .flatMap(loadMapTile)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loadedMapTilesSubject);

        loadedMapTiles = loadedMapTilesSubject;
        this.mapTiles = mapTilesSubject;
        offset = tempOffset.observeOn(AndroidSchedulers.mainThread());
    }

    private PointD calculateOffset(final Integer zoomLevel,
                                   final PointD viewSize,
                                   final LatLng center) {
        final double mapPxSize = coordinateProjection.pxSize(zoomLevel);
        final PointD centerPx = getPointCoord(center);
        final double offsetX2 = centerPx.x - (mapPxSize / 2.0);
        final double offsetY2 = centerPx.y - (mapPxSize / 2.0);
        final double centerOffsetX = (viewSize.x - mapPxSize) / 2.0;
        final double centerOffsetY = (viewSize.y - mapPxSize) / 2.0;
        final double offsetX = centerOffsetX - offsetX2;
        final double offsetY = centerOffsetY - offsetY2;
        Log.d(TAG, "offsetPx(" + offsetX + ", " + offsetY + ")");
        return new PointD(offsetX, offsetY);
    }

    public Observable<Collection<MapTile>> getMapTiles() {
        return mapTiles;
    }

    public Observable<MapTile> getLoadedMapTiles() {
        return loadedMapTiles;
    }

    final private Func1<MapTile, Observable<MapTile>> loadMapTile =
            new Func1<MapTile, Observable<MapTile>>() {
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

    public void zoomIn() {
        zoomLevel.setZoomLevel(
                Math.min(20, zoomLevel.getZoomLevel() + 1));
    }

    public void zoomOut() {
        zoomLevel.setZoomLevel(
                Math.max(0, zoomLevel.getZoomLevel() - 1));
    }

    static private <T> Action1<T> logOnNext(final String tag) {
        return new Action1<T>() {
            @Override
            public void call(T value) {
                Log.d(TAG, tag + ": " + value);
            }
        };
    }

    public void setViewSize(int width, int height) {
        viewSize.onNext(new PointD(width, height));
    }

    public PointD getPointCoord(final LatLng latLng) {
        return coordinateProjection.fromLatLngToPoint(
                latLng.getLat(), latLng.getLng(), zoomLevel.getZoomLevel());
    }

    public Observable<PointD> getOffset() {
        return offset;
    }

    public int getTileSizePx() {
        return mapNetworkAdapter.getTileSizePx();
    }
}
