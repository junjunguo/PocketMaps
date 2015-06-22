package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ZoomButton;

import com.junjunguo.pocketmaps.R;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.MapViewPosition;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 22, 2015.
 */
public class SideBarController {
    protected ImageButton showPositionImgBtn;
    protected ZoomButton zoomInBtn, zoomOutBtn;
    private int ZOOM_LEVEL_MAX;
    private int ZOOM_LEVEL_MIN;

    /**
     * init and implement btn functions
     */
    public SideBarController(Activity activity, MapView mapView, int zoom_level_max, int zoom_level_min) {
        this.showPositionImgBtn = (ImageButton) activity.findViewById(R.id.show_my_position_btn);
        this.zoomInBtn = (ZoomButton) activity.findViewById(R.id.zoom_in_btn);
        this.zoomOutBtn = (ZoomButton) activity.findViewById(R.id.zoom_out_btn);
        this.ZOOM_LEVEL_MAX = zoom_level_max;
        this.ZOOM_LEVEL_MIN = zoom_level_min;
        showMyLocation(activity, mapView);
        zoomControlHandler(mapView);
    }


    /**
     * implement zoom btn
     */
    protected void zoomControlHandler(final MapView mapView) {
        zoomInBtn.setImageResource(R.drawable.zoom_in);
        zoomOutBtn.setImageResource(R.drawable.zoom_out);

        zoomInBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                MapViewPosition mvp = mapView.getModel().mapViewPosition;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        zoomInBtn.setImageResource(R.drawable.zoom_in_f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        zoomInBtn.setImageResource(R.drawable.zoom_in);
                        if (mvp.getZoomLevel() < ZOOM_LEVEL_MAX) mvp.zoomIn();
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
                        zoomOutBtn.setImageResource(R.drawable.zoom_out_f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        zoomOutBtn.setImageResource(R.drawable.zoom_out);
                        if (mvp.getZoomLevel() > ZOOM_LEVEL_MIN) mvp.zoomOut();
                        return true;
                }

                return false;
            }
        });
    }


    /**
     * move map to my current location as the center of the screen
     */
    protected void showMyLocation(final Activity activity, final MapView mapView) {
        showPositionImgBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        showPositionImgBtn.setImageResource(R.drawable.show_position_f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (MapActivity.mCurrentLocation != null) {
                            showPositionImgBtn.setImageResource(R.drawable.show_position);
                            mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
                                    new LatLong(MapActivity.mCurrentLocation.getLatitude(),
                                            MapActivity.mCurrentLocation.getLongitude()), (byte) 16));
                        } else {
                            showPositionImgBtn.setImageResource(R.drawable.show_position_invisible);
                            Toast.makeText(activity, "No Location Available", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }

                return false;
            }
        });
    }
}
