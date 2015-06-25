package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.util.ItemData;
import com.junjunguo.pocketmaps.model.util.NavigatorListener;
import com.junjunguo.pocketmaps.model.util.SettingsAdapter;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.MapViewPosition;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 24, 2015.
 */
public class MapActions implements NavigatorListener {
    private Activity activity;
    private int ZOOM_LEVEL_MAX;
    private int ZOOM_LEVEL_MIN;
    protected FloatingActionButton showPositionBtn, navigationBtn, settingsBtn, controlBtn;
    protected FloatingActionButton zoomInBtn, zoomOutBtn;
    private ViewGroup sideBarVP, sideBarMenuVP, navSettingsVP, navInstructionVP, nvaInstructionListVP;
    private boolean menuVisible;

    public MapActions(Activity activity, MapView mapView, int zoom_level_max, int zoom_level_min) {
        this.activity = activity;
        this.showPositionBtn = (FloatingActionButton) activity.findViewById(R.id.map_show_my_position_fab);
        this.navigationBtn = (FloatingActionButton) activity.findViewById(R.id.map_nav_fab);
        this.settingsBtn = (FloatingActionButton) activity.findViewById(R.id.map_settings_fab);
        this.controlBtn = (FloatingActionButton) activity.findViewById(R.id.map_sidebar_control_afb);
        this.zoomInBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_in_fab);
        this.zoomOutBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_out_fab);
        this.ZOOM_LEVEL_MAX = zoom_level_max;
        this.ZOOM_LEVEL_MIN = zoom_level_min;
        this.sideBarVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_layout);
        this.sideBarMenuVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_menu_layout);
        this.navSettingsVP = (ViewGroup) activity.findViewById(R.id.nav_settings_layout);
        this.navInstructionVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_layout);
        this.nvaInstructionListVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_list_layout);
        menuVisible = false;
        controlBtnHandler();

        zoomControlHandler(mapView);
        showMyLocation(mapView);
        navBtnHandler();
        navSettingsHandler();
    }

    /**
     * navigation settings implementation
     */
    private void navSettingsHandler() {
        final ImageButton navSettingsClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_clear_btn);
        navSettingsClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.INVISIBLE);
                sideBarVP.setVisibility(View.VISIBLE);
            }
        });
    }


    /**
     * handler clicks on nav button
     */
    private void navBtnHandler() {
        RecyclerView navSettingsRV = (RecyclerView) activity.findViewById(R.id.nav_settings_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        navSettingsRV.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
        navSettingsRV.setLayoutManager(mLayoutManager);

        ItemData from = new ItemData(R.drawable.ic_location_start_white_24dp, "from", "home");
        ItemData to = new ItemData(R.drawable.ic_location_end_white_24dp, "to", "school");
        ItemData[] myDataset = {from, to};
        RecyclerView.Adapter mAdapter = new SettingsAdapter(myDataset);
        navSettingsRV.setAdapter(mAdapter);

        navSettingsRV.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                rv.getChildAdapterPosition(childView);

                return false;
            }

            @Override public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });


        navigationBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                sideBarVP.setVisibility(View.INVISIBLE);
                navSettingsVP.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * start button: control button handler
     */
    private void controlBtnHandler() {
        final ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1);
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        anim.setFillEnabled(true);
        anim.setDuration(300);
        anim.setInterpolator(new OvershootInterpolator());

        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isMenuVisible()) {
                    setMenuVisible(false);
                    sideBarMenuVP.setVisibility(View.INVISIBLE);
                    controlBtn.setImageResource(R.drawable.ic_keyboard_arrow_up_white_24dp);
                    controlBtn.startAnimation(anim);
                } else {
                    setMenuVisible(true);
                    sideBarMenuVP.setVisibility(View.VISIBLE);
                    controlBtn.setImageResource(R.drawable.ic_clear_white_24dp);
                    controlBtn.startAnimation(anim);
                }
            }
        });
    }

    /**
     * implement zoom btn
     */
    protected void zoomControlHandler(final MapView mapView) {
        zoomInBtn.setImageResource(R.drawable.ic_add_white_24dp);
        zoomOutBtn.setImageResource(R.drawable.ic_remove_white_24dp);

        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            MapViewPosition mvp = mapView.getModel().mapViewPosition;

            @Override public void onClick(View v) {
                if (mvp.getZoomLevel() < ZOOM_LEVEL_MAX) mvp.zoomIn();
            }
        });
        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            MapViewPosition mvp = mapView.getModel().mapViewPosition;

            @Override public void onClick(View v) {
                if (mvp.getZoomLevel() > ZOOM_LEVEL_MIN) mvp.zoomOut();
            }
        });
    }

    /**
     * move map to my current location as the center of the screen
     */
    protected void showMyLocation(final MapView mapView) {
        showPositionBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (MapActivity.mCurrentLocation != null) {
                    showPositionBtn.setImageResource(R.drawable.ic_my_location_white_24dp);
                    mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
                            new LatLong(MapActivity.mCurrentLocation.getLatitude(),
                                    MapActivity.mCurrentLocation.getLongitude()),
                            mapView.getModel().mapViewPosition.getZoomLevel()));
                } else {
                    showPositionBtn.setImageResource(R.drawable.ic_location_searching_white_24dp);
                    Toast.makeText(activity, "No Location Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * @return side bar menu visibility status
     */
    public boolean isMenuVisible() {
        return menuVisible;
    }

    /**
     * side bar menu visibility
     *
     * @param menuVisible
     */
    public void setMenuVisible(boolean menuVisible) {
        this.menuVisible = menuVisible;
    }

    /**
     * the change on navigator: navigation is used or not
     *
     * @param on
     */
    @Override public void statusChanged(boolean on) {
        if (on) {
            navigationBtn.setImageResource(R.drawable.ic_directions_white_24dp);
        } else {
            navigationBtn.setImageResource(R.drawable.ic_navigation_white_24dp);
        }
    }
}
