package com.tehmou.rxmaps.view;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import com.tehmou.rxmaps.pojo.MapTileDrawable;
import com.tehmou.rxmaps.utils.CoordinateProjection;
import com.tehmou.rxmaps.utils.LatLng;
import com.tehmou.rxmaps.pojo.ZoomLevel;
import com.tehmou.rxmaps.utils.LatLngCalculator;
import com.tehmou.rxmaps.utils.Logger;
import com.tehmou.rxmaps.utils.MapState;
import com.tehmou.rxmaps.utils.MapTileUtils;
import com.tehmou.rxmaps.utils.PointD;
import com.tehmou.rxmaps.utils.RxFilters;
import com.tehmou.rxmaps.utils.TileBitmapLoader;
import com.tehmou.rxmaps.utils.TouchDeltaListener;

import java.util.Collection;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapViewModel {
    private static final String TAG = MapViewModel.class.getCanonicalName();
    final private Observable<Collection<MapTileDrawable>> mapTiles;
    final private ZoomLevel zoomLevel;
    final private Subject<PointD, PointD> viewSize;
    final private Subject<LatLng, LatLng> centerCoordSubject;
    final private Observable<LatLng> centerCoord;
    final private CoordinateProjection coordinateProjection;
    final private Subject<PointD, PointD> dragDelta;
    final private Subject<Boolean, Boolean> isDragging;

    final private Observable<Map<Integer, Bitmap>> loadedTileBitmapsObservable;

    public MapViewModel(final int tileSizePx, final TileBitmapLoader tileBitmapLoader) {
        dragDelta = PublishSubject.create();
        isDragging = PublishSubject.create();
        zoomLevel = new ZoomLevel(4);
        viewSize = PublishSubject.create();
        centerCoordSubject = BehaviorSubject.create(new LatLng(51.507351, -0.127758));
        coordinateProjection = new CoordinateProjection(tileSizePx);

        final LatLngCalculator latLngCalculator = new LatLngCalculator(
                coordinateProjection, dragDelta);

        centerCoord = Observable.merge(
                centerCoordSubject,
                latLngCalculator.getObservable())
                .distinctUntilChanged();

        final BehaviorSubject<Collection<MapTileDrawable>> mapTilesSubject =
                BehaviorSubject.create();
        final PublishSubject<Map<Integer, Bitmap>> loadedTileBitmapsSubject =
                PublishSubject.create();

        Observable<MapState> mapStateObservable =
                Observable.combineLatest(
                        zoomLevel.getObservable()
                                .distinctUntilChanged()
                                .doOnNext(zoomLevel -> Log.d(TAG, "zoomLevel=" + zoomLevel)),
                        viewSize
                                .distinctUntilChanged()
                                .doOnNext(viewSize -> Log.d(TAG, "viewSize=" + viewSize)),
                        centerCoord
                                .distinctUntilChanged()
                                .doOnNext(centerCoord -> Log.d(TAG, "centerCoord=" + centerCoord)),
                        MapTileUtils.combineToMapState(coordinateProjection))
                        .distinctUntilChanged()
                        .doOnNext(mapState -> Log.d(TAG, "mapState=" + mapState))
                .cache();

        latLngCalculator.setMapStateObservable(mapStateObservable);

        Log.d(TAG, "Create mapTiles observable");
        final Observable<Collection<MapTileDrawable>> mapTiles =
                mapStateObservable
                        .map(MapTileUtils.calculateMapTiles(tileSizePx));

        mapTiles
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mapTilesSubject);

        Pair<Boolean, Collection<MapTileDrawable>> initialValue =
                new Pair<>(Boolean.FALSE, (Collection<MapTileDrawable>)null);

        Observable<Pair<Boolean, Collection<MapTileDrawable>>> mapTilesDraggingPair =
                Observable.combineLatest(
                        Observable.merge(
                                zoomLevel.getObservable().map(zoomLevel -> true),
                                isDragging.map(_isDragging -> !_isDragging))
                                .doOnNext(isShowingMapTiles ->
                                        Log.d(TAG, "isShowingMapTiles=" + isShowingMapTiles)),
                        mapTiles, Pair::new);

        Observable<Map<Integer, Bitmap>> loadedMapTileObservable =
                Observable.concat(Observable.just(initialValue), mapTilesDraggingPair)
                        .flatMap(pair -> Observable.just(pair, initialValue))
                        .doOnNext(pair -> Log.d(TAG, "isTure=" + pair.first))
                        .distinctUntilChanged(booleanCollectionPair -> booleanCollectionPair.first)
                        .filter(booleanCollectionPair -> booleanCollectionPair.first)
                        .doOnNext(booleanCollectionPair -> {
                            Log.d(TAG, "Updating tiles");
                        })
                        .map(booleanCollectionPair -> booleanCollectionPair.second)
                        .flatMap(tileBitmapLoader);

        loadedMapTileObservable
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

    public void setTouchDeltaEvents(Observable<TouchDeltaListener.TouchDeltaEvent> touchDeltaEvents) {
        touchDeltaEvents.map(TouchDeltaListener.TouchDeltaEvent::getDelta)
                .filter(RxFilters.nullFilter())
                .subscribe(dragDelta);
        touchDeltaEvents.map(touchDeltaEvent ->
                    !touchDeltaEvent.getType()
                            .equals(TouchDeltaListener.TouchDeltaEvent.TouchDeltaEventType.END))
                .distinctUntilChanged()
                .subscribe(isDragging);
    }
}
