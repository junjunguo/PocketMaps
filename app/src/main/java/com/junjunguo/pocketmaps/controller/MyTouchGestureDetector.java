package com.junjunguo.pocketmaps.controller;

import android.view.ViewConfiguration;

import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.input.TouchGestureDetector;
import org.mapsforge.map.util.MapViewProjection;

/**
 * This file is part of Offline Map
 * <p>
 * Created by GuoJunjun <junjunguo.com> on June 17, 2015.
 */
public class MyTouchGestureDetector extends TouchGestureDetector {
//    public MyTouchGestureDetector(MapView mapView, ViewConfiguration viewConfiguration) {
//        super(mapView, viewConfiguration);
//    }

    private final float doubleTapSlop;

    private final int gestureTimeout;
    private Point lastActionUpPoint;
    private long lastEventTime;
    private final MyMapView mapView;
    private final MapViewProjection projection;

    public MyTouchGestureDetector(MyMapView mapView, ViewConfiguration viewConfiguration) {
        super(null,viewConfiguration);
        this.mapView = mapView;
        this.doubleTapSlop = viewConfiguration.getScaledDoubleTapSlop();
        this.gestureTimeout = ViewConfiguration.getDoubleTapTimeout();
        this.projection = new MapViewProjection(this.mapView);
    }
}
