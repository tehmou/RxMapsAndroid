package com.tehmou.rxmaps.utils;

import android.view.MotionEvent;
import android.view.View;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by ttuo on 29/08/14.
 */
public class ViewUtils {
    private ViewUtils() { }

    static public class TouchDelta implements View.OnTouchListener {
        private Subject<PointD, PointD> deltaStream = PublishSubject.create();
        private PointD lastTouch = null;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouch = new PointD(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (lastTouch != null) {
                        final PointD delta = new PointD(event.getX() - lastTouch.x, event.getY() - lastTouch.y);
                        deltaStream.onNext(delta);
                        lastTouch = new PointD(event.getX(), event.getY());
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    lastTouch = null;
                    break;
            }
            return true;
        }

        public Observable<PointD> getObservable() {
            return deltaStream;
        }
    }
}
