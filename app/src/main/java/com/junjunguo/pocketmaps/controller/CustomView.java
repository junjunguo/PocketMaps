package com.junjunguo.pocketmaps.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.input.TouchGestureDetector;
import org.mapsforge.map.controller.MapViewController;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.model.Model;
import org.mapsforge.map.scalebar.MapScaleBar;
import org.mapsforge.map.view.FpsCounter;
import org.mapsforge.map.view.FrameBuffer;

/**
 * This file is part of Pocket Maps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 17, 2015.
 */
public class CustomView extends ViewGroup implements org.mapsforge.map.view.MapView{
    private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;

//    private final FpsCounter fpsCounter;
//    private final FrameBuffer frameBuffer;
//    private final FrameBufferController frameBufferController;
    private GestureDetector gestureDetector;
//    private final LayerManager layerManager;
    private MapScaleBar mapScaleBar;
//    private final MapZoomControls mapZoomControls;
    private final Model model;
//    private final TouchEventHandler touchEventHandler;

    public CustomView(Context context) {
        this(context, null);
    }

    public CustomView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setWillNotDraw(false);

        this.model = new Model();

//        this.fpsCounter = new FpsCounter(GRAPHIC_FACTORY, this.model.displayModel);
//        this.frameBuffer = new FrameBuffer(this.model.frameBufferModel, this.model.displayModel, GRAPHIC_FACTORY);
//        this.frameBufferController = FrameBufferController.create(this.frameBuffer, this.model);

//        this.layerManager = new LayerManager(this, this.model.mapViewPosition, GRAPHIC_FACTORY);
//        this.layerManager.start();
//        LayerManagerController.create(this.layerManager, this.model);

        MapViewController.create(this, this.model);

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
//        ScaleGestureDetector sgd = new ScaleGestureDetector(context, new ScaleListener(this.getModel().mapViewPosition));
        TouchGestureDetector touchGestureDetector = new TouchGestureDetector(null, viewConfiguration);
//        this.touchEventHandler = new TouchEventHandler(null, viewConfiguration, sgd);
//        this.touchEventHandler.addListener(touchGestureDetector);
//        this.mapZoomControls = new MapZoomControls(context, null);
//        this.mapScaleBar = new DefaultMapScaleBar(this.model.mapViewPosition, this.model.mapViewDimension,
//                GRAPHIC_FACTORY, this.model.displayModel);
//        ((DefaultMapScaleBar) this.mapScaleBar).setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
    }

    /**
     * {@inheritDoc}
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override public void destroy() {

    }

    @Override public Dimension getDimension() {
        return null;
    }

    @Override public FpsCounter getFpsCounter() {
        return null;
    }

    @Override public MapScaleBar getMapScaleBar() {
        return null;
    }

    @Override public void setMapScaleBar(MapScaleBar mapScaleBar) {

    }

    /**
     * @return the FrameBuffer used in this MapView.
     */
    @Override public FrameBuffer getFrameBuffer() {
        return null;
    }

    @Override public LayerManager getLayerManager() {
        return null;
    }

    @Override public Model getModel() {
        return null;
    }

    /**
     * Requests a redrawing as soon as possible.
     */
    @Override public void repaint() {

    }
}
