package com.tehmou.rxmaps.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by ttuo on 03/09/14.
 */
public class RxMapsContentProvider extends ContentProvider {
    private static final String DATA_PROVIDER_AUTHORITY = "com.tehmou.rxmaps.provider";


    final public static int MAP_TILE_BITMAPS = 10;
    final public static Uri MAP_TILE_BITMAPS_CONTENT_URI =
            Uri.parse("content://" + DATA_PROVIDER_AUTHORITY + "/" + MapTileBitmapsTable.TABLE_NAME);

    final public static UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(DATA_PROVIDER_AUTHORITY, MapTileBitmapsTable.TABLE_NAME, MAP_TILE_BITMAPS);
    }

    private RxMapsDatabaseHelper database;

    @Override
    public boolean onCreate() {
        database = new RxMapsDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
