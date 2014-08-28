package com.tehmou.rxmaps.view;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.util.Pair;

import com.tehmou.rxmaps.network.MapNetworkAdapter;
import com.tehmou.rxmaps.utils.CoordinateProjection;
import com.tehmou.rxmaps.utils.LatLng;
import com.tehmou.rxmaps.pojo.MapTile;
import com.tehmou.rxmaps.pojo.MapTileLoaded;
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
    final private Observable<MapTileLoaded> loadedMapTiles;
    final private ZoomLevel zoomLevel;
    final private Subject<Pair<Integer, Integer>, Pair<Integer, Integer>> viewSize;
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
        final Subject<MapTileLoaded, MapTileLoaded> loadedMapTilesSubject =
                PublishSubject.create();

        final Observable<Collection<MapTile>> mapTiles =
                Observable.combineLatest(
                        zoomLevel.getObservable()
                                .doOnNext(logOnNext("zoomLevel")),
                        viewSize
                                .doOnNext(logPairOnNext("viewSize")),
                        Observable.from(mapNetworkAdapter.getTileSizePx())
                                .doOnNext(logOnNext("tileSizePx")),
                        centerCoord,
                        new Func4<Integer, Pair<Integer, Integer>, Integer, LatLng, Collection<MapTile>>() {
                            @Override
                            public Collection<MapTile> call(final Integer zoomLevel,
                                                            final Pair<Integer, Integer> viewSize,
                                                            final Integer tileSizePx,
                                                            final LatLng center) {
                                final int mapPxSize = coordinateProjection.pxSize(zoomLevel);
                                final PointD centerPx = getPointCoord(center);

                                //final double offsetX = (viewSize.first / 2.0) - centerPx.x;
                                //final double offsetY = (viewSize.second / 2.0) - centerPx.y;
                                final double centerOffsetX = (viewSize.first - mapPxSize) / 2.0;
                                final double centerOffsetY = (viewSize.second - mapPxSize) / 2.0;
                                final double offsetX = centerOffsetX;
                                final double offsetY = centerOffsetY;
                                Log.d(TAG, "offsetPx(" + offsetX + ", " + offsetY + ")");

                                final int firstTileX = (int) Math.floor(-offsetX / tileSizePx);
                                final int firstTileY = (int) Math.floor(-offsetY / tileSizePx);
                                final int numX = (int) Math.ceil(viewSize.first / tileSizePx);
                                final int numY = (int) Math.ceil(viewSize.second / tileSizePx);

                                final List<MapTile> mapTileList = new ArrayList<MapTile>();
                                for (int i = firstTileX; i <= firstTileX + numX; i++) {
                                    for (int n = firstTileY; n <= firstTileY + numY; n++) {
                                        final MapTile mapTile = new MapTile(
                                                zoomLevel, i, n,
                                                i*tileSizePx + offsetX,
                                                n*tileSizePx + offsetY);
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
    }

    public void subscribe() {

    }

    public void unsubscribe() {

    }

    public Observable<Collection<MapTile>> getMapTiles() {
        return mapTiles;
    }

    public Observable<MapTileLoaded> getLoadedMapTiles() {
        return loadedMapTiles;
    }

    final private Func1<MapTile, Observable<MapTileLoaded>> loadMapTile =
            new Func1<MapTile, Observable<MapTileLoaded>>() {
                @Override
                public Observable<MapTileLoaded> call(final MapTile mapTile) {
                    return mapNetworkAdapter.getMapTile(
                            mapTile.getZoom(), mapTile.getX(), mapTile.getY())
                            .map(new Func1<Bitmap, MapTileLoaded>() {
                                @Override
                                public MapTileLoaded call(Bitmap bitmap) {
                                    return new MapTileLoaded(mapTile, bitmap);
                                }
                            })
                            .onErrorResumeNext(new Func1<Throwable, Observable<? extends MapTileLoaded>>() {
                                @Override
                                public Observable<? extends MapTileLoaded> call(Throwable throwable) {
                                    return Observable.from(new MapTileLoaded(mapTile, null));
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

    static private Action1<Pair<Integer, Integer>> logPairOnNext(final String tag) {
        return new Action1<Pair<Integer, Integer>>() {
            @Override
            public void call(Pair<Integer, Integer> value) {
                Log.d(TAG, tag + ": " + value.first + ", " + value.second);
            }
        };
    }

    public void setViewSize(int width, int height) {
        viewSize.onNext(new Pair<Integer, Integer>(width, height));
    }

    public PointD getPointCoord(final LatLng latLng) {
        return coordinateProjection.fromLatLngToPoint(
                latLng.getLat(), latLng.getLng(), zoomLevel.getZoomLevel());
    }
}
