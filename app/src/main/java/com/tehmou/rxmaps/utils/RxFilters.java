package com.tehmou.rxmaps.utils;

import rx.functions.Func1;

/**
 * Created by ttuo on 12/09/14.
 */
public class RxFilters {
    static public<T> Func1<T, Boolean> nullFilter() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T t) {
                return t != null;
            }
        };
    }
}
