package com.tehmou.rxmaps.view;

import android.graphics.Bitmap;

import com.tehmou.rxmaps.network.MapNetworkAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapViewModel {
    final private MapNetworkAdapter mapNetworkAdapter;
    final private Subject<Collection<MapTile>, Collection<MapTile>> mapTiles;
    final private Observable<MapTileLoaded> loadedMapTiles;

    public MapViewModel(final MapNetworkAdapter mapNetworkAdapter) {
        this.mapNetworkAdapter = mapNetworkAdapter;

        mapTiles = BehaviorSubject.create((Collection<MapTile>) new ArrayList<MapTile>());
        mapTiles.onNext(Arrays.asList(new MapTile(0, 0, 0, 0, 0)));

        loadedMapTiles = mapTiles
                .flatMap(new Func1<Collection<MapTile>, Observable<MapTile>>() {
                    @Override
                    public Observable<MapTile> call(Collection<MapTile> mapTiles) {
                        return Observable.from(mapTiles);
                    }
                })
                .flatMap(loadMapTile)
                .observeOn(AndroidSchedulers.mainThread());
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
                            });
                }
            };
}
