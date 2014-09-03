package com.tehmou.rxmaps.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ttuo on 03/09/14.
 */
public class RxMapsDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = RxMapsDatabaseHelper.class.getCanonicalName();
    public static final String DATABASE_NAME = "com.tehmou.rxmaps";
    private static final int DATABASE_VERSION = 1;

    public RxMapsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        onCreate(getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        MapTileBitmapsTable.create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        MapTileBitmapsTable.drop(db);
        MapTileBitmapsTable.create(db);
    }
}
