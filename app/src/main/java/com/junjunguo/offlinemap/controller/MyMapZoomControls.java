package com.junjunguo.offlinemap.controller;

import android.content.Context;

import org.mapsforge.map.android.input.MapZoomControls;
import org.mapsforge.map.android.view.MapView;

/**
 * extends from Mapsforge MzpZoomControls used to define my own zoom controls
 * <p>
 * This file is part of Offline Map
 * <p>
 * Created by GuoJunjun <junjunguo.com> on June 16, 2015.
 */
public class MyMapZoomControls extends MapZoomControls {
    private final MyZoomControls zoomControls;

    public MyMapZoomControls(Context context, MapView mapView) {
        super(context, mapView);
        this.zoomControls = new MyZoomControls(context);
    }
}
