package com.tehmou.rxmaps.utils;

import android.util.Log;

import com.tehmou.rxmaps.pojo.ZoomLevel;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by ttuo on 29/08/14.
 */
public class LatLngCalculator {
    private static final String TAG = LatLngCalculator.class.getCanonicalName();
    final private Subject<LatLng, LatLng> observable;
    private LatLng lastLatLng;
    private MapState mapState;

    public LatLngCalculator(final CoordinateProjection coordinateProjection,
                            final Observable<PointD> pixelDelta,
                            final Observable<LatLng> latLng) {
        final Subject<LatLng, LatLng> latLngSubject = BehaviorSubject.create();
        pixelDelta.subscribe(new Action1<PointD>() {
            @Override
            public void call(final PointD pixedDelta) {
                Log.v(TAG, "pixelDelta(" + pixedDelta + ")");
                final double cx = mapState.viewSize.x/2.0 - mapState.offset.x;
                final double cy = mapState.viewSize.y/2.0 - mapState.offset.y;
                final PointD newPoint = new PointD(cx - pixedDelta.x, cy - pixedDelta.y);
                final LatLng newLatLng = coordinateProjection.fromPointToLatLng(newPoint, mapState.zoomLevel);
                latLngSubject.onNext(newLatLng);
            }
        });
        latLng.subscribe(new Action1<LatLng>() {
            @Override
            public void call(LatLng latLng) {
                LatLngCalculator.this.lastLatLng = latLng;
                latLngSubject.onNext(latLng);
            }
        });
        observable = latLngSubject;
    }

    public void setMapStateObservable(final Observable<MapState> mapStateObservable) {
        mapStateObservable.subscribe(new Action1<MapState>() {
            @Override
            public void call(MapState mapState) {
                LatLngCalculator.this.mapState = mapState;
            }
        });
    }

    public Observable<LatLng> getObservable() {
        return observable;
    }
}
