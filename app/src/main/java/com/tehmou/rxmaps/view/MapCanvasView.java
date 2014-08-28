package com.tehmou.rxmaps.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tehmou.rxmaps.pojo.MapTile;
import com.tehmou.rxmaps.utils.PointD;

import java.util.ArrayList;
import java.util.Collection;

import rx.functions.Action1;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapCanvasView extends View {
    private static final String TAG = MapCanvasView.class.getCanonicalName();
    private Paint paint;
    private Paint rectPaint;
    private MapViewModel viewModel;

    private Collection<MapTile> mapTiles;
    final private Collection<MapTile> loadedMapTiles = new ArrayList<MapTile>();
    private PointD offset;
    private int tileSize;

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
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
    }

    public void setViewModel(final MapViewModel mapViewModel) {
        this.viewModel = mapViewModel;
        this.tileSize = mapViewModel.getTileSizePx();
        mapViewModel.getMapTiles().subscribe(setMapTiles);
        mapViewModel.getLoadedMapTiles().subscribe(setLoadedMapTile);
        mapViewModel.getOffset().subscribe(setOffset);
    }

    final private Action1<Collection<MapTile>> setMapTiles =
            new Action1<Collection<MapTile>>() {
                @Override
                public void call(Collection<MapTile> mapTiles) {
                    Log.d(TAG, "setMapTiles(" + mapTiles + ")");
                    MapCanvasView.this.mapTiles = mapTiles;
                    invalidate();
                }
            };

    final private Action1<MapTile> setLoadedMapTile =
            new Action1<MapTile>() {
                @Override
                public void call(final MapTile mapTile) {
                    Log.d(TAG, "setLoadedMapTile(" + mapTile + ")");
                    loadedMapTiles.add(mapTile);
                    invalidate();
                }
            };

    final private Action1<PointD> setOffset =
            new Action1<PointD>() {
                @Override
                public void call(PointD offset) {
                    MapCanvasView.this.offset = offset;
                    invalidate();
                }
            };

    @Override
    protected void onDraw(Canvas canvas) {
        if (mapTiles == null) {
            return;
        }
        for (MapTile mapTile : mapTiles) {
            MapTile mapTileLoaded = null;
            for (MapTile t : loadedMapTiles) {
                if (mapTile.getX() == t.getX() &&
                        mapTile.getY() == t.getY() &&
                        mapTile.getZoom() == t.getZoom()) {
                    mapTileLoaded = t;
                }
            }
            if (mapTileLoaded != null && mapTileLoaded.getBitmap() != null) {
                final float x = (float) (mapTile.getX() * tileSize + offset.x);
                final float y = (float) (mapTile.getY() * tileSize + offset.y);
                canvas.drawBitmap(mapTileLoaded.getBitmap(), x, y, paint);
                canvas.drawRect(
                        x, y,
                        x + mapTileLoaded.getBitmap().getWidth() - 1,
                        y + mapTileLoaded.getBitmap().getHeight() - 1,
                        rectPaint);
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
