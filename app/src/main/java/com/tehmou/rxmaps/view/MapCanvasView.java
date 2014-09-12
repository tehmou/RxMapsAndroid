package com.tehmou.rxmaps.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tehmou.rxmaps.pojo.MapTileDrawable;
import com.tehmou.rxmaps.utils.PointD;
import com.tehmou.rxmaps.utils.RxFilters;
import com.tehmou.rxmaps.utils.TouchDeltaListener;

import java.util.Collection;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapCanvasView extends View {
    private static final String TAG = MapCanvasView.class.getCanonicalName();
    private Paint paint;
    private Paint rectPaint;
    private Paint textPaint;
    private MapViewModel viewModel;

    private Collection<MapTileDrawable> mapTiles;
    private Map<Integer, Bitmap> mapTileBitmaps;
    final private Observable<PointD> touchDelta;

    public MapCanvasView(Context context) {
        this(context, null);
    }

    public MapCanvasView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        TouchDeltaListener touchDeltaListener = new TouchDeltaListener();
        setOnTouchListener(touchDeltaListener);
        this.touchDelta = touchDeltaListener.getObservable().map(
                new Func1<TouchDeltaListener.TouchDeltaEvent, PointD>() {
                    @Override
                    public PointD call(TouchDeltaListener.TouchDeltaEvent touchDeltaEvent) {
                        return touchDeltaEvent.getDelta();
                    }
                })
                .filter(RxFilters.nullFilter());
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.setBackgroundColor(Color.LTGRAY);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    public void setViewModel(final MapViewModel mapViewModel) {
        this.viewModel = mapViewModel;
        mapViewModel.setTouchDelta(touchDelta);
        mapViewModel.getMapTiles().subscribe(setMapTiles);
        mapViewModel.getMapTileBitmaps().subscribe(setMapTileBitmaps);
    }

    final private Action1<Collection<MapTileDrawable>> setMapTiles =
            new Action1<Collection<MapTileDrawable>>() {
                @Override
                public void call(Collection<MapTileDrawable> mapTiles) {
                    Log.d(TAG, "setMapTiles(" + mapTiles + ")");
                    MapCanvasView.this.mapTiles = mapTiles;
                    invalidate();
                }
            };

    final private Action1<Map<Integer, Bitmap>> setMapTileBitmaps =
            new Action1<Map<Integer, Bitmap>>() {
                @Override
                public void call(final Map<Integer, Bitmap> mapTileBitmaps) {
                    Log.d(TAG, "setMapTileBitmaps(" + mapTileBitmaps + ")");
                    MapCanvasView.this.mapTileBitmaps = mapTileBitmaps;
                    invalidate();
                }
            };

    @Override
    protected void onDraw(final Canvas canvas) {
        if (mapTiles == null) {
            return;
        }
        for (MapTileDrawable mapTile : mapTiles) {
            final int hash = mapTile.tileHashCode();

            final float x = (float) mapTile.getScreenX();
            final float y = (float) mapTile.getScreenY();
            final Bitmap bitmap = mapTileBitmaps != null ? mapTileBitmaps.get(hash) : null;
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, x, y, paint);
            } else {
                Log.d(TAG, "Loaded bitmap was null: " + mapTile);
            }
            canvas.drawRect(
                    x, y,
                    x + (float) mapTile.getSize() - 1,
                    y + (float) mapTile.getSize() - 1,
                    rectPaint);
            canvas.drawText(mapTile.getX() + ", " + mapTile.getY(), x + 3, y + 20, textPaint);
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
