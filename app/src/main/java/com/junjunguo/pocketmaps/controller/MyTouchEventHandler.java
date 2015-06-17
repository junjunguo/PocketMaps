package com.junjunguo.pocketmaps.controller;

import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;

import org.mapsforge.map.android.input.TouchEventHandler;
import org.mapsforge.map.android.view.MapView;

/**
 * This file is part of Offline Map
 * <p>
 * Created by GuoJunjun <junjunguo.com> on June 17, 2015.
 */
public class MyTouchEventHandler extends TouchEventHandler {
    public MyTouchEventHandler(MapView mapView, ViewConfiguration viewConfiguration, ScaleGestureDetector sgd) {
        super(mapView, viewConfiguration, sgd);
    }


}
