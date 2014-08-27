package com.tehmou.rxmaps.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;

import rx.functions.Action1;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapCanvasView extends View {
    private static final String TAG = MapCanvasView.class.getCanonicalName();
    private Paint paint;
    final private Collection<MapTileLoaded> mapTiles = new ArrayList<MapTileLoaded>();
    private MapViewModel viewModel;

    public MapCanvasView(Context context) {
        this(context, null);
    }

    public MapCanvasView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.setBackgroundColor(Color.BLUE);
    }

    public void setViewModel(final MapViewModel mapViewModel) {
        this.viewModel = mapViewModel;
        mapViewModel.getMapTiles().subscribe(setLoadedMapTile);
    }

    final private Action1<MapTileLoaded> setLoadedMapTile =
            new Action1<MapTileLoaded>() {
                @Override
                public void call(MapTileLoaded mapTile) {
                    mapTiles.add(mapTile);
                    invalidate();
                }
            };

    @Override
    protected void onDraw(Canvas canvas) {
        for (MapTileLoaded mapTile : mapTiles) {
            if (mapTile.getBitmap() != null) {
                canvas.drawBitmap(mapTile.getBitmap(),
                        mapTile.getScreenX(), mapTile.getScreenY(), paint);
            } else {
                Log.d(TAG, "Error loading tile: " + mapTile);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (viewModel != null) {
            viewModel.setViewSize(right - left, bottom - top);
        }
    }
}
