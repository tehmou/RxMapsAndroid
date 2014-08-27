package com.tehmou.rxmaps.view;

import android.graphics.Bitmap;
import android.util.Log;

import com.tehmou.rxmaps.network.MapNetworkAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
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

    public MapViewModel(final MapNetworkAdapter mapNetworkAdapter) {
        this.mapNetworkAdapter = mapNetworkAdapter;

        final Subject<MapTileLoaded, MapTileLoaded> loadedMapTilesSubject =
                PublishSubject.create();

        zoomLevel = new ZoomLevel(0);
        mapTiles = zoomLevel
                .getObservable()
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer zoomLevel) {
                        Log.d(TAG, "zoomLevel: " + zoomLevel);
                    }
                })
                .map(new Func1<Integer, Collection<MapTile>>() {
                    @Override
                    public Collection<MapTile> call(Integer zoomLevel) {
                        return Arrays.asList(new MapTile(zoomLevel, 0, 0, 0, 0));
                    }
                });

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

    static private class ZoomLevel {
        final private Subject<Integer, Integer> subject;
        private int zoomLevel;

        public ZoomLevel(int zoomLevel) {
            this.zoomLevel = zoomLevel;
            subject = BehaviorSubject.create(this.zoomLevel);
        }

        public Observable<Integer> getObservable() {
            return subject;
        }

        public void setZoomLevel(int zoomLevel) {
            this.zoomLevel = zoomLevel;
            subject.onNext(zoomLevel);
        }

        public int getZoomLevel() {
            return zoomLevel;
        }
    }
}
