package com.tehmou.rxmaps.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;

import rx.functions.Action1;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapView extends View {
    private Paint paint;
    final private Collection<MapTileLoaded> mapTiles = new ArrayList<MapTileLoaded>();

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.setBackgroundColor(Color.BLUE);
    }

    public void setViewModel(final MapViewModel mapViewModel) {
        mapViewModel.getMapTiles().subscribe(setLoadedMapTile);
    }

    final private Action1<MapTileLoaded> setLoadedMapTile =
            new Action1<MapTileLoaded>() {
                @Override
                public void call(MapTileLoaded mapTile) {
                    mapTiles.add(mapTile);
                }
            };

    @Override
    protected void onDraw(Canvas canvas) {
        for (MapTileLoaded mapTile : mapTiles) {
            canvas.drawBitmap(mapTile.getBitmap(),
                    mapTile.getScreenX(), mapTile.getScreenY(), paint);
        }
    }
}
