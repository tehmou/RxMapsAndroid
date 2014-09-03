package com.tehmou.rxmaps.view;

import android.util.Log;

import com.tehmou.rxmaps.network.MapNetworkAdapter;
import com.tehmou.rxmaps.pojo.MapTileBitmap;
import com.tehmou.rxmaps.pojo.MapTileDrawable;
import com.tehmou.rxmaps.utils.CoordinateProjection;
import com.tehmou.rxmaps.utils.LatLng;
import com.tehmou.rxmaps.pojo.MapTile;
import com.tehmou.rxmaps.pojo.ZoomLevel;
import com.tehmou.rxmaps.utils.LatLngCalculator;
import com.tehmou.rxmaps.utils.MapState;
import com.tehmou.rxmaps.utils.MapTileUtils;
import com.tehmou.rxmaps.utils.PointD;

import java.util.Collection;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapViewModel {
    private static final String TAG = MapViewModel.class.getCanonicalName();
    final private MapNetworkAdapter mapNetworkAdapter;
    final private Observable<Collection<MapTileDrawable>> mapTiles;
    final private Observable<MapTileBitmap> loadedMapTiles;
    final private ZoomLevel zoomLevel;
    final private Subject<PointD, PointD> viewSize;
    final private Subject<LatLng, LatLng> centerCoordSubject;
    final private Observable<LatLng> centerCoord;
    final private CoordinateProjection coordinateProjection;
    final private Subject<PointD, PointD> dragDelta;

    private PointD lastViewSize = null;

    public MapViewModel(final MapNetworkAdapter mapNetworkAdapter) {
        this.mapNetworkAdapter = mapNetworkAdapter;
        dragDelta = PublishSubject.create();
        zoomLevel = new ZoomLevel(0);
        viewSize = PublishSubject.create();
        centerCoordSubject = BehaviorSubject.create(new LatLng(51.507351, -0.127758));
        coordinateProjection = new CoordinateProjection(mapNetworkAdapter.getTileSizePx());

        final LatLngCalculator latLngCalculator = new LatLngCalculator(
                coordinateProjection, dragDelta, centerCoordSubject);

        centerCoord = Observable.merge(
                centerCoordSubject,
                latLngCalculator.getObservable())
                .distinctUntilChanged();

        final Subject<Collection<MapTileDrawable>, Collection<MapTileDrawable>> mapTilesSubject =
                BehaviorSubject.create();
        final Subject<MapTileBitmap, MapTileBitmap> loadedMapTilesSubject =
                PublishSubject.create();

        Observable<MapState> mapStateObservable =
                Observable.combineLatest(
                        zoomLevel.getObservable().doOnNext(MapTileUtils.logOnNext("zoomLevel")),
                        viewSize.doOnNext(MapTileUtils.logOnNext("viewSize")),
                        centerCoord.doOnNext(MapTileUtils.logOnNext("centerCoord")),
                        MapTileUtils.combineToMapState(coordinateProjection)
                )
                .cache();

        latLngCalculator.setMapStateObservable(mapStateObservable);

        final Observable<Collection<MapTileDrawable>> mapTiles =
                mapStateObservable
                        .map(MapTileUtils.calculateMapTiles(mapNetworkAdapter.getTileSizePx()));

        mapTiles
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mapTilesSubject);

        mapTiles
                .flatMap(MapTileUtils.expandCollection)
                .flatMap(MapTileUtils.loadMapTile(mapNetworkAdapter))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loadedMapTilesSubject);

        loadedMapTiles = loadedMapTilesSubject;
        this.mapTiles = mapTilesSubject;
    }

    public Observable<Collection<MapTileDrawable>> getMapTiles() {
        return mapTiles;
    }

    public Observable<MapTileBitmap> getLoadedMapTiles() {
        return loadedMapTiles;
    }

    public void zoomIn() {
        zoomLevel.setZoomLevel(
                Math.min(20, zoomLevel.getZoomLevel() + 1));
    }

    public void zoomOut() {
        zoomLevel.setZoomLevel(
                Math.max(0, zoomLevel.getZoomLevel() - 1));
    }

    public void setViewSize(int width, int height) {
        viewSize.onNext(new PointD(width, height));
        lastViewSize = new PointD(width, height);
    }

    public void setTouchDelta(Observable<PointD> touchDelta) {
        touchDelta.subscribe(dragDelta);
    }
}
