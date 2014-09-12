package com.tehmou.rxmaps.utils;

import android.view.MotionEvent;
import android.view.View;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by ttuo on 29/08/14.
 */
public class TouchDeltaListener implements View.OnTouchListener {
    private Subject<TouchDeltaEvent, TouchDeltaEvent> eventSubject = PublishSubject.create();
    private PointD lastTouch = null;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouch = new PointD(event.getX(), event.getY());
                eventSubject.onNext(
                        new TouchDeltaEvent(TouchDeltaEvent.TouchDeltaEventType.START, null));
                break;
            case MotionEvent.ACTION_MOVE:
                if (lastTouch != null) {
                    final PointD delta = new PointD(event.getX() - lastTouch.x, event.getY() - lastTouch.y);
                    eventSubject.onNext(
                            new TouchDeltaEvent(TouchDeltaEvent.TouchDeltaEventType.MOVE, delta));
                    lastTouch = new PointD(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                lastTouch = null;
                eventSubject.onNext(
                        new TouchDeltaEvent(TouchDeltaEvent.TouchDeltaEventType.END, null));
                break;
        }
        return true;
    }

    public Observable<TouchDeltaEvent> getObservable() {
            return eventSubject;
        }

    public static class TouchDeltaEvent {
        public enum TouchDeltaEventType {
            START, MOVE, END
        }
        final private TouchDeltaEventType type;
        final private PointD delta;

        public TouchDeltaEvent(TouchDeltaEventType type, PointD delta) {
            this.type = type;
            this.delta = delta;
        }

        public TouchDeltaEventType getType() {
            return type;
        }

        public PointD getDelta() {
            return delta;
        }
    }
}
