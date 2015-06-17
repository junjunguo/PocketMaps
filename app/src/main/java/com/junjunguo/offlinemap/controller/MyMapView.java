package com.junjunguo.offlinemap.controller;

import android.content.Context;
import android.util.AttributeSet;

import org.mapsforge.map.android.view.MapView;

/**
 * extends from Mapsforge MapView used to define own MapView layout
 * <p/>
 * This file is part of Offline Map
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 16, 2015.
 */
public class MyMapView extends MapView {
    private final MyMapZoomControls mapZoomControls;

    public MyMapView(Context context) {
        this(context, null);
    }


    public MyMapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mapZoomControls = new MyMapZoomControls(context, this);
    }
}
