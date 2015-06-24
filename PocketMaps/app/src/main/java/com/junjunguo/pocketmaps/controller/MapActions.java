package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.view.ViewGroup;
import android.widget.ZoomButton;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.util.NavigatorListener;

import org.mapsforge.map.android.view.MapView;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on June 24, 2015.
 */
public class MapActions implements NavigatorListener {
    private Activity activity;
    private int ZOOM_LEVEL_MAX;
    private int ZOOM_LEVEL_MIN;
    protected FloatingActionButton showPositionBtn, navigationBtn, settingsBtn, controlBtn;
    protected ZoomButton zoomInBtn, zoomOutBtn;
    private ViewGroup sideBarVP,navSettingsVP, navInstructionVP,nvaInstructionListVP;

    public MapActions(Activity activity, MapView mapView, int zoom_level_max, int zoom_level_min) {
        this.activity = activity;
        this.showPositionBtn = (FloatingActionButton) activity.findViewById(R.id.show_my_position_btn);
        this.navigationBtn = (FloatingActionButton) activity.findViewById(R.id.navigation_btn);
        this.settingsBtn = (FloatingActionButton) activity.findViewById(R.id.settings_btn);
        this.controlBtn = controlBtn;
        this.zoomInBtn = (ZoomButton) activity.findViewById(R.id.zoom_in_btn);
        this.zoomOutBtn = (ZoomButton) activity.findViewById(R.id.zoom_out_btn);
        this.ZOOM_LEVEL_MAX = zoom_level_max;
        this.ZOOM_LEVEL_MIN = zoom_level_min;
        this.activity = activity;
        this.sideBarVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_layout);
        this.navSettingsVP = (ViewGroup) activity.findViewById(R.id.nav_settings_layout);
        this.navInstructionVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_layout);
        this.nvaInstructionListVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_list_layout);
    }

    /**
     * the change on navigator
     *
     * @param on
     */
    @Override public void statusChanged(boolean on) {

    }
}
