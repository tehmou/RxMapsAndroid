package com.tehmou.rxmaps.utils;

import rx.functions.Func1;

/**
 * Created by ttuo on 12/09/14.
 */
public class RxFilters {
    public static<T> Func1<T, Boolean> nullFilter() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T t) {
                return t != null;
            }
        };
    }

    public static Func1<Boolean, Boolean> isFalseFilter() {
        return new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean t) {
                return !t;
            }
        };
    }

    public static Func1<Boolean, Boolean> isTrueFilter() {
        return new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean t) {
                return t;
            }
        };
    }
}
