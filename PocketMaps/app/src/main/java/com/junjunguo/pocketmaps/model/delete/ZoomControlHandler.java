package com.junjunguo.pocketmaps.model.delete;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ZoomButton;

import com.junjunguo.pocketmaps.R;

import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.MapViewPosition;

/**
 * A singleton class used for zoom control
 * <p/>
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 18, 2015.
 */
public class ZoomControlHandler {
    /**
     * maximum zoom level.
     */
    private final int ZOOM_LEVEL_MAX;

    /**
     * minimum zoom level.
     */
    private final int ZOOM_LEVEL_MIN;
    private int currentZoomLevel;
    private MapView mapView;
    private Activity activity;
    private ZoomButton zoomInBtn;
    private ZoomButton zoomOutBtn;


    private ZoomControlHandler(MapView mapView, Activity context) {
        this.ZOOM_LEVEL_MAX = mapView.getMapZoomControls().getZoomLevelMax();
        this.ZOOM_LEVEL_MIN = mapView.getMapZoomControls().getZoomLevelMin();
        this.activity = context;
        this.mapView = mapView;
        zoomControlHandler();
    }

    private static ZoomControlHandler instance = null;

    public ZoomControlHandler getInstance(MapView mapView, Activity activity) {
        if (instance == null) {
            return new ZoomControlHandler(mapView, activity);
        } else {
            return instance;
        }
    }

    /**
     * current zoom level
     *
     * @param currentZoomLevel
     */
    public void setCurrentZoomLevel(int currentZoomLevel) {
        this.currentZoomLevel = currentZoomLevel;
    }

    public void zoomIn() {

    }

    /**
     * implement zoom btn
     */
    private void zoomControlHandler() {
         zoomInBtn = (ZoomButton) activity.findViewById(R.id.map_zoom_in_fab);
         zoomOutBtn = (ZoomButton) activity.findViewById(R.id.map_zoom_out_fab);


//        zoomInBtn.setImageResource(R.drawable.zoom_in);
//        zoomOutBtn.setImageResource(R.drawable.zoom_out);

        zoomInBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                MapViewPosition mvp = mapView.getModel().mapViewPosition;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        zoomInBtn.setImageResource(R.drawable.zoom_in_f);
                        return true;
                    case MotionEvent.ACTION_UP:
//                        zoomInBtn.setImageResource(R.drawable.zoom_in);
                        if (mvp.getZoomLevel() < mvp.getZoomLevelMax() - 3) mvp.zoomIn();
                        return true;
                }
                return false;
            }
        });
        zoomOutBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                MapViewPosition mvp = mapView.getModel().mapViewPosition;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        zoomOutBtn.setImageResource(R.drawable.zoom_out_f);
                        return true;
                    case MotionEvent.ACTION_UP:
//                        zoomOutBtn.setImageResource(R.drawable.zoom_out);
                        if (mvp.getZoomLevel() > mvp.getZoomLevelMin()) mvp.zoomOut();
                        return true;
                }

                return false;
            }
        });
    }

}
