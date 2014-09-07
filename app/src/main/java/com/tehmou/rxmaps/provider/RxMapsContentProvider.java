package com.tehmou.rxmaps.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.tehmou.rxmaps.pojo.MapTile;
import com.tehmou.rxmaps.pojo.MapTileBitmap;

/**
 * Created by ttuo on 03/09/14.
 */
public class RxMapsContentProvider extends ContentProvider {
    private static final String TAG = RxMapsContentProvider.class.getCanonicalName();
    private static final String DATA_PROVIDER_AUTHORITY = "com.tehmou.rxmaps.provider";

    public static final int MAP_TILE_BITMAPS_ID = 10;
    public static final Uri MAP_TILE_BITMAPS_CONTENT_URI =
            Uri.parse("content://" + DATA_PROVIDER_AUTHORITY + "/" + MapTileBitmapsTable.TABLE_NAME);

    public static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(DATA_PROVIDER_AUTHORITY, MapTileBitmapsTable.TABLE_NAME + "/#/#/#", MAP_TILE_BITMAPS_ID);
    }

    private RxMapsDatabaseHelper database;

    @Override
    public boolean onCreate() {
        database = new RxMapsDatabaseHelper(getContext());
        return false;
    }

    static private String getTableName(Uri uri) {
        int uriType = sURIMatcher.match(uri);
        String tableName = null;
        switch (uriType) {
            case MAP_TILE_BITMAPS_ID:
                tableName = MapTileBitmapsTable.TABLE_NAME;
                break;
        }
        return tableName;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final String tableName = getTableName(uri);
        final int zoom = Integer.valueOf(uri.getPathSegments().get(1));
        final int x = Integer.valueOf(uri.getPathSegments().get(2));
        final int y = Integer.valueOf(uri.getPathSegments().get(3));
        Log.d(TAG, "query(" + tableName + "," + zoom + "," + x + "," + y + ")");

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(tableName);
        final String where = MapTileBitmapsTable.COLUMN_X + "='" + x + "'"
                + " AND " + MapTileBitmapsTable.COLUMN_Y + "='" + y + "'"
                + " AND " + MapTileBitmapsTable.COLUMN_ZOOM_LEVEL + "='" + zoom + "'";
        queryBuilder.appendWhere(where);
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final String tableName = getTableName(uri);
        final int zoom = Integer.valueOf(uri.getPathSegments().get(1));
        final int x = Integer.valueOf(uri.getPathSegments().get(2));
        final int y = Integer.valueOf(uri.getPathSegments().get(3));
        Log.d(TAG, "insert(" + tableName + "," + zoom + "," + x + "," + y + ")");

        final SQLiteDatabase db = database.getWritableDatabase();
        values.put(MapTileBitmapsTable.COLUMN_ZOOM_LEVEL, zoom);
        values.put(MapTileBitmapsTable.COLUMN_X, x);
        values.put(MapTileBitmapsTable.COLUMN_Y, y);
        db.insert(tableName, null, values);
        return uri;
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
