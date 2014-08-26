package com.tehmou.rxmaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapView extends View {
    private Bitmap bitmap;
    private Paint paint;

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
        mapViewModel.getBitmap().subscribe(setBitmap);
    }

    final private Action1<Bitmap> setBitmap = new Action1<Bitmap>() {
        @Override
        public void call(Bitmap bitmap) {
            MapView.this.bitmap = bitmap;
            invalidate();
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }
    }
}
