package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ZoomButton;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.Navigator;
import com.junjunguo.pocketmaps.model.util.NavigatorListener;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.MapViewPosition;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 22, 2015.
 */
public class SideBarController implements NavigatorListener {
    protected ImageButton showPositionBtn, navigationBtn, settingsBtn;
    protected ZoomButton zoomInBtn, zoomOutBtn;
    private int ZOOM_LEVEL_MAX;
    private int ZOOM_LEVEL_MIN;
    private Activity activity;

    private boolean drawerOpen;
    private ViewGroup navSettingsView, navInstructionView;


    /**
     * init and implement btn functions
     */
    public SideBarController(Activity activity, MapView mapView, int zoom_level_max, int zoom_level_min) {
        this.activity = activity;
        this.showPositionBtn = (ImageButton) activity.findViewById(R.id.show_my_position_btn);
        this.navigationBtn = (ImageButton) activity.findViewById(R.id.navigation_btn);
        this.settingsBtn = (ImageButton) activity.findViewById(R.id.settings_btn);
        this.zoomInBtn = (ZoomButton) activity.findViewById(R.id.zoom_in_btn);
        this.zoomOutBtn = (ZoomButton) activity.findViewById(R.id.zoom_out_btn);
        this.ZOOM_LEVEL_MAX = zoom_level_max;
        this.ZOOM_LEVEL_MIN = zoom_level_min;
        this.drawerOpen = false;
        this.navSettingsView = (ViewGroup) activity.findViewById(R.id.nav_settings_layout);
        this.navInstructionView = (ViewGroup) activity.findViewById(R.id.nav_instruction_layout);
        showMyLocation(activity, mapView);
        zoomControlHandler(mapView);
        navigationHandler(false);
        settingsHandler(activity);


    }

    /**
     * Handler navigation
     *
     * @param on navigation on or off (in use or not in use)
     */
    protected void navigationHandler(Boolean on) {
        navSettings();
        if (on) {
            navigatorOnActions();
        } else {
            navigatorOffActions();
        }
    }


    /**
     * navigation function not in use
     */
    private void navigatorOffActions() {
        navigationBtn.setImageResource(R.drawable.ic_navigation_black_48dp);
        navigationBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (drawerOpen) {
                            navigationBtn.setImageResource(R.drawable.ic_navigate_next_black_48dp);
                        } else {
                            navigationBtn.setImageResource(R.drawable.ic_navigate_before_black_48dp);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (drawerOpen) {
                            navigationBtn.setImageResource(R.drawable.ic_navigation_black_48dp);
                            drawerOpen = false;
                            navSettingsView.setVisibility(View.INVISIBLE);
                        } else {
                            navigationBtn.setImageResource(R.drawable.ic_navigate_next_black_48dp);
                            drawerOpen = true;
                            navSettingsView.setVisibility(View.VISIBLE);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * navigation settings view
     */
    private void navSettings() {
        startNavVehicleSettings();
        startNavWeightingSettings();
        startNavDirectionSettings();
    }


    private void startNavVehicleSettings() {
        final ImageButton footImgBtn = (ImageButton) activity.findViewById(R.id.nav_settings_foot);
        final ImageButton bikeImgBtn = (ImageButton) activity.findViewById(R.id.nav_settings_bike);
        final ImageButton carImgBtn = (ImageButton) activity.findViewById(R.id.nav_settings_car);
        //init image
        footImgBtn.setImageResource(R.drawable.ic_directions_walk_black_48dp);
        bikeImgBtn.setImageResource(R.drawable.ic_directions_bike_light_36dp);
        carImgBtn.setImageResource(R.drawable.ic_directions_car_light_36dp);
        //actions
        footImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Navigator.getNavigator().setVehicle("foot");
                footImgBtn.setImageResource(R.drawable.ic_directions_walk_black_48dp);
                bikeImgBtn.setImageResource(R.drawable.ic_directions_bike_light_36dp);
                carImgBtn.setImageResource(R.drawable.ic_directions_car_light_36dp);
            }
        });
        bikeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Navigator.getNavigator().setVehicle("bike");
                footImgBtn.setImageResource(R.drawable.ic_directions_walk_light_36dp);
                bikeImgBtn.setImageResource(R.drawable.ic_directions_bike_black_48dp);
                carImgBtn.setImageResource(R.drawable.ic_directions_car_light_36dp);
            }
        });
        carImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Navigator.getNavigator().setVehicle("car");
                footImgBtn.setImageResource(R.drawable.ic_directions_walk_light_36dp);
                bikeImgBtn.setImageResource(R.drawable.ic_directions_bike_light_36dp);
                carImgBtn.setImageResource(R.drawable.ic_directions_car_black_48dp);
            }
        });
    }

    private void startNavWeightingSettings() {
        final ImageButton fastestImgBtn = (ImageButton) activity.findViewById(R.id.nav_settings_fastest);
        final ImageButton shortestImgBtn = (ImageButton) activity.findViewById(R.id.nav_settings_shortest);
        //init image
        fastestImgBtn.setImageResource(R.drawable.ic_trending_up_black_48dp);
        shortestImgBtn.setImageResource(R.drawable.ic_trending_neutral_light_36dp);
        //actions
        fastestImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Navigator.getNavigator().setWeighting("fastest");
                fastestImgBtn.setImageResource(R.drawable.ic_trending_up_black_48dp);
                shortestImgBtn.setImageResource(R.drawable.ic_trending_neutral_light_36dp);

            }
        });
        shortestImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Navigator.getNavigator().setWeighting("shortest");
                fastestImgBtn.setImageResource(R.drawable.ic_trending_up_light_36dp);
                shortestImgBtn.setImageResource(R.drawable.ic_trending_neutral_black_48dp);

            }
        });

    }

    private void startNavDirectionSettings() {
        CheckBox useCurrentLocalCB = (CheckBox) activity.findViewById(R.id.nav_settings_use_currentLocal);
        ImageButton startPositionImgBtn = (ImageButton) activity.findViewById(R.id.nav_settings_start);
        ImageButton endPositionImgBtn = (ImageButton) activity.findViewById(R.id.nav_settings_end);
        //init image

        startPositionImgBtn.setImageResource(R.drawable.ic_location_on_light_36dp);
        endPositionImgBtn.setImageResource(R.drawable.ic_pin_drop_black_36dp);
    }

    /**
     * navigation function is in use
     */
    private void navigatorOnActions() {

    }

    //    protected void showHideInstructionHandler(Activity activity, MapView mapView) {
    //        if
    //        showHideInstructionBtn.setImageResource(R.drawable.ic_settings_black_48dp);
    //        showHideInstructionBtn.setOnTouchListener(new View.OnTouchListener() {
    //            @Override public boolean onTouch(View v, MotionEvent event) {
    //                switch (event.getAction()) {
    //                    case MotionEvent.ACTION_DOWN:
    //                        showHideInstructionBtn.setImageResource(R.drawable.ic_settings_applications_black_48dp);
    //                        return true;
    //                    case MotionEvent.ACTION_UP:
    //                        showHideInstructionBtn.setImageResource(R.drawable.ic_settings_black_48dp);
    //                        ///
    //                        //        activity.startActivity(new Intent(activity,));
    //                        return true;
    //                }
    //
    //                return false;
    //            }
    //        });
    //    }

    protected void settingsHandler(Activity activity) {
        settingsBtn.setImageResource(R.drawable.ic_settings_black_48dp);
        settingsBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        settingsBtn.setImageResource(R.drawable.ic_settings_applications_black_48dp);
                        return true;
                    case MotionEvent.ACTION_UP:
                        settingsBtn.setImageResource(R.drawable.ic_settings_black_48dp);
                        ///
                        //        activity.startActivity(new Intent(activity,));
                        return true;
                }

                return false;
            }
        });
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
        showPositionBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        showPositionBtn.setImageResource(R.drawable.show_position_f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (MapActivity.mCurrentLocation != null) {
                            showPositionBtn.setImageResource(R.drawable.show_position);
                            mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
                                    new LatLong(MapActivity.mCurrentLocation.getLatitude(),
                                            MapActivity.mCurrentLocation.getLongitude()), (byte) 16));
                        } else {
                            showPositionBtn.setImageResource(R.drawable.show_position_invisible);
                            Toast.makeText(activity, "No Location Available", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return false;
            }
        });
    }


    /**
     * the change on navigator
     *
     * @param on
     */
    @Override public void statusChanged(boolean on) {
        navigationHandler(on);
    }
}
