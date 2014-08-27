package com.tehmou.rxmaps.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.tehmou.rxmaps.R;

/**
 * Created by ttuo on 27/08/14.
 */
public class MapView extends FrameLayout {
    private MapCanvasView mapCanvasView;
    private MapViewModel viewModel;

    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setViewModel(final MapViewModel mapViewModel) {
        this.viewModel = mapViewModel;
        mapCanvasView.setViewModel(mapViewModel);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mapCanvasView = (MapCanvasView) findViewById(R.id.rx_map_view_canvas);
        findViewById(R.id.rx_map_view_zoom_in)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.zoomIn();
                    }
                });
        findViewById(R.id.rx_map_view_zoom_out)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.zoomOut();
                    }
                });
    }
}
