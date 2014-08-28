package com.tehmou.rxmaps.view;

import com.tehmou.rxmaps.network.MapNetworkAdapter;
import com.tehmou.rxmaps.utils.CoordinateProjection;
import com.tehmou.rxmaps.utils.LatLng;
import com.tehmou.rxmaps.pojo.MapTile;
import com.tehmou.rxmaps.pojo.ZoomLevel;
import com.tehmou.rxmaps.utils.MapState;
import com.tehmou.rxmaps.utils.MapTileUtils;
import com.tehmou.rxmaps.utils.PointD;

import java.util.Collection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
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

        Observable<MapState> mapStateObservable =
                Observable.combineLatest(
                        zoomLevel.getObservable().doOnNext(MapTileUtils.logOnNext("zoomLevel")),
                        viewSize.doOnNext(MapTileUtils.logOnNext("viewSize")),
                        centerCoord.doOnNext(MapTileUtils.logOnNext("centerCoord")),
                        MapTileUtils.combineToMapState(coordinateProjection)
                )
                .cache();

        final Observable<Collection<MapTile>> mapTiles =
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
        offset = mapStateObservable
                .map(new Func1<MapState, PointD>() {
                    @Override
                    public PointD call(MapState mapState) {
                        return mapState.offset;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Collection<MapTile>> getMapTiles() {
        return mapTiles;
    }

    public Observable<MapTile> getLoadedMapTiles() {
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
    }

    public Observable<PointD> getOffset() {
        return offset;
    }

    public int getTileSizePx() {
        return mapNetworkAdapter.getTileSizePx();
    }
}
