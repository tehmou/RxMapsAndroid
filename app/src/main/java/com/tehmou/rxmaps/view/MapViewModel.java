package com.tehmou.rxmaps.view;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import com.tehmou.rxmaps.network.MapNetworkAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapViewModel {
    private static final String TAG = MapViewModel.class.getCanonicalName();
    final private MapNetworkAdapter mapNetworkAdapter;
    final private Observable<MapTileLoaded> loadedMapTiles;
    final private ZoomLevel zoomLevel;
    final private Subject<Pair<Integer, Integer>, Pair<Integer, Integer>> viewSize;

    public MapViewModel(final MapNetworkAdapter mapNetworkAdapter) {
        this.mapNetworkAdapter = mapNetworkAdapter;
        zoomLevel = new ZoomLevel(0);
        viewSize = PublishSubject.create();

        final Subject<MapTileLoaded, MapTileLoaded> loadedMapTilesSubject =
                PublishSubject.create();

        final Observable<Collection<MapTile>> mapTiles =
                Observable.combineLatest(
                        zoomLevel.getObservable()
                                .doOnNext(logOnNext("zoomLevel")),
                        viewSize
                                .doOnNext(logPairOnNext("viewSize")),
                        new Func2<Integer, Pair<Integer, Integer>, Collection<MapTile>>() {
                            @Override
                            public Collection<MapTile> call(Integer zoomLevel,
                                                            Pair<Integer, Integer> viewSize) {
                                return Arrays.asList(new MapTile(zoomLevel, 0, 0, 0, 0));
                            }
                        }
                );

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
    }

    public void subscribe() {

    }

    public void unsubscribe() {

    }

    public Observable<MapTileLoaded> getMapTiles() {
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
}