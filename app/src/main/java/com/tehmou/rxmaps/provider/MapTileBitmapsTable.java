package com.tehmou.rxmaps.provider;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ttuo on 03/09/14.
 */
public class MapTileBitmapsTable {
    public final static String TABLE_NAME = "map_tile_bitmaps";

    private static final String COLUMN_DB_ID = "_id";
    public static final String COLUMN_X = "x";
    public static final String COLUMN_Y = "y";
    public static final String COLUMN_ZOOM_LEVEL = "zoom_level";
    public static final String COLUMN_BITMAP_FILENAME = "bitmap_filename";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String[] PROJECTION = new String[] {
            COLUMN_X, COLUMN_Y, COLUMN_ZOOM_LEVEL, COLUMN_BITMAP_FILENAME };

    private MapTileBitmapsTable () { }

    static private String getDatabaseCreate() {
        return "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME
                + "("
                + COLUMN_DB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + ", "
                + COLUMN_X + " INTEGER"
                + ", "
                + COLUMN_Y + " INTEGER"
                + ", "
                + COLUMN_ZOOM_LEVEL + " INTEGER"
                + ", "
                + COLUMN_BITMAP_FILENAME + " STRING"
                + ", "
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
    }

    static private String getDatabaseDrop() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    static public void create(final SQLiteDatabase db) {
        db.execSQL(getDatabaseCreate());
    }

    static public void drop(final SQLiteDatabase db) {
        db.execSQL(getDatabaseDrop());
    }
}
