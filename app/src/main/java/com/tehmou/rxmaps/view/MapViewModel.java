package com.tehmou.rxmaps.view;

import android.graphics.Bitmap;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscriber;
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
    final private Observable<Collection<MapTileDrawable>> mapTiles;
    final private ZoomLevel zoomLevel;
    final private Subject<PointD, PointD> viewSize;
    final private Subject<LatLng, LatLng> centerCoordSubject;
    final private Observable<LatLng> centerCoord;
    final private CoordinateProjection coordinateProjection;
    final private Subject<PointD, PointD> dragDelta;

    final private Map<Integer, Bitmap> loadedTileBitmaps = new ConcurrentHashMap<Integer, Bitmap>();
    final private Observable<Map<Integer, Bitmap>> loadedTileBitmapsObservable;

    public MapViewModel(final MapNetworkAdapter mapNetworkAdapter) {
        this.mapNetworkAdapter = mapNetworkAdapter;
        dragDelta = PublishSubject.create();
        zoomLevel = new ZoomLevel(3);
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
        final Subject<Map<Integer, Bitmap>, Map<Integer, Bitmap>> loadedTileBitmapsSubject =
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
                .filter(new Func1<MapTileDrawable, Boolean>() {
                    @Override
                    public Boolean call(final MapTileDrawable mapTileDrawable) {
                        return !loadedTileBitmaps.containsKey(mapTileDrawable.tileHashCode());
                    }
                })
                .flatMap(MapTileUtils.loadMapTile(mapNetworkAdapter))
                .map(new Func1<MapTileBitmap, Map<Integer, Bitmap>>() {
                    @Override
                    public Map<Integer, Bitmap> call(final MapTileBitmap mapTileBitmap) {
                        if (mapTileBitmap != null && mapTileBitmap.getBitmap() != null) {
                            loadedTileBitmaps.put(mapTileBitmap.getTileHashCode(),
                                    mapTileBitmap.getBitmap());
                        }
                        return loadedTileBitmaps;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loadedTileBitmapsSubject);

        loadedTileBitmapsObservable = loadedTileBitmapsSubject;
        this.mapTiles = mapTilesSubject;
    }

    public Observable<Collection<MapTileDrawable>> getMapTiles() {
        return mapTiles;
    }

    public Observable<Map<Integer, Bitmap>> getMapTileBitmaps() {
        return loadedTileBitmapsObservable;
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
    }

    public void setTouchDelta(Observable<PointD> touchDelta) {
        touchDelta.subscribe(dragDelta);
    }
}
