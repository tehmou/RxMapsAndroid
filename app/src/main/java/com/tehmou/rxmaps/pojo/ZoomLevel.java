package com.tehmou.rxmaps.pojo;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * Created by ttuo on 27/08/14.
 */
public class ZoomLevel {
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
