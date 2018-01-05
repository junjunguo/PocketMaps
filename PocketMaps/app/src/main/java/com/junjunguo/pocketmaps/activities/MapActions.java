package com.junjunguo.pocketmaps.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.fragments.AppSettings;
import com.junjunguo.pocketmaps.map.Destination;
import com.junjunguo.pocketmaps.model.listeners.MapHandlerListener;
import com.junjunguo.pocketmaps.model.listeners.NavigatorListener;
import com.junjunguo.pocketmaps.model.listeners.OnClickAddressListener;
import com.junjunguo.pocketmaps.navigator.NaviEngine;
import com.junjunguo.pocketmaps.map.MapHandler;
import com.junjunguo.pocketmaps.map.Navigator;
import com.junjunguo.pocketmaps.fragments.InstructionAdapter;
import com.junjunguo.pocketmaps.util.MyUtility;
import com.junjunguo.pocketmaps.util.Variable;
import org.oscim.android.MapView;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;

/**
 * This file is part of PocketMaps
 * <p>
 * menu controller, controls menus for map activity
 * <p>
 * Created by GuoJunjun <junjunguo.com> on June 24, 2015.
 */
public class MapActions implements NavigatorListener, MapHandlerListener {
    enum TabAction{ StartPoint, EndPoint, None };
    private TabAction tabAction = TabAction.None;
    private Activity activity;
    private AppSettings appSettings;
    protected FloatingActionButton showPositionBtn, navigationBtn, settingsBtn, controlBtn;
    protected FloatingActionButton zoomInBtn, zoomOutBtn;
    private ViewGroup sideBarVP, sideBarMenuVP, navSettingsVP, navSettingsFromVP, navSettingsToVP, // navInstructionVP,
            navInstructionListVP, navTopVP;
    private boolean menuVisible;
    private EditText fromLocalET, toLocalET;

    public MapActions(Activity activity, MapView mapView) {
        this.activity = activity;
        this.showPositionBtn = (FloatingActionButton) activity.findViewById(R.id.map_show_my_position_fab);
        this.navigationBtn = (FloatingActionButton) activity.findViewById(R.id.map_nav_fab);
        this.settingsBtn = (FloatingActionButton) activity.findViewById(R.id.map_settings_fab);
        this.controlBtn = (FloatingActionButton) activity.findViewById(R.id.map_sidebar_control_fab);
        this.zoomInBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_in_fab);
        this.zoomOutBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_out_fab);
        // view groups managed by separate layout xml file : //map_sidebar_layout/map_sidebar_menu_layout
        this.sideBarVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_layout);
        this.sideBarMenuVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_menu_layout);
        this.navSettingsVP = (ViewGroup) activity.findViewById(R.id.nav_settings_layout);
        this.navTopVP = (ViewGroup) activity.findViewById(R.id.navtop_layout);
        this.navSettingsFromVP = (ViewGroup) activity.findViewById(R.id.nav_settings_from_layout);
        this.navSettingsToVP = (ViewGroup) activity.findViewById(R.id.nav_settings_to_layout);
        this.navInstructionListVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_list_layout);
        //form location and to location textView
        this.fromLocalET = (EditText) activity.findViewById(R.id.nav_settings_from_local_et);
        this.toLocalET = (EditText) activity.findViewById(R.id.nav_settings_to_local_et);
        this.menuVisible = false;
        MapHandler.getMapHandler().setMapHandlerListener(this);
        Navigator.getNavigator().addListener(this);
        appSettings = new AppSettings(activity);
        initControlBtnHandler();
        initZoomControlHandler(mapView);
        initShowMyLocation(mapView);
        initNavBtnHandler();
        initNavSettingsHandler();
        initSettingsBtnHandler();
    }

    /**
     * init and implement performance for settings
     */
    private void initSettingsBtnHandler() {
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                appSettings.showAppSettings(sideBarVP);
            }
        });
    }
    
    /**
     * navigation settings implementation
     * <p>
     * settings clear button
     * <p>
     * settings search button
     */
    private void initNavSettingsHandler() {
        final ImageButton navSettingsClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_clear_btn);
        navSettingsClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.INVISIBLE);
                sideBarVP.setVisibility(View.VISIBLE);
            }
        });
        ImageButton navSettingsSearchBtn = (ImageButton) activity.findViewById(R.id.nav_settings_search_btn);
        navSettingsSearchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchBtnActions();
            }
        });
        initTravelModeSetting();
        initSettingsFromItemHandler();
        initSettingsToItemHandler();
    }

    /**
     * perform actions when search btn is clicked
     * <p>
     * 1. check edit text and convert value to start point or end point
     */
    private void searchBtnActions() {
        String fls = fromLocalET.getText().toString();
        String tls = toLocalET.getText().toString();
        GeoPoint fl = null, tl = null;
        if (fls.length() > 2) {
            fl = MyUtility.getLatLong(fls);
        }
        if (tls.length() > 2) {
            tl = MyUtility.getLatLong(tls);
        }
        if (fl != null && tl == null) {
            MapHandler.getMapHandler().centerPointOnMap(fl, 0, 0, 0);
            Destination.getDestination().setStartPoint(fl);
            addFromMarker(fl);
        }
        if (fl == null && tl != null) {
            MapHandler.getMapHandler().centerPointOnMap(tl, 0, 0, 0);
            Destination.getDestination().setEndPoint(tl);
            addToMarker(tl);
        }
        if (fl != null && tl != null) {
            addFromMarker(fl);
            addToMarker(tl);
            Destination.getDestination().setStartPoint(fl);
            Destination.getDestination().setEndPoint(tl);
            activeNavigator();
        }
        if (fl == null && tl == null) {
            Toast.makeText(activity,
                    "Check your input (use coordinates)!\nExample:\nuse degrees: 63° 25′ 47″ N, 10° 23′ 36″ " +
                            "E\nor use digital: 63.429722, 10.393333", Toast.LENGTH_LONG).show();
        }
    }
    
    @SuppressWarnings("deprecation")
    private void setBgColor(ViewGroup vg, int color)
    {
      vg.setBackgroundColor(activity.getResources().getColor(color));
    }

    /**
     * settings layout:
     * <p>
     * to item handler: when to item is clicked
     */
    private void initSettingsToItemHandler() {
        final ViewGroup toItemVG = (ViewGroup) activity.findViewById(R.id.map_nav_settings_to_item);
        toItemVG.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBgColor(toItemVG, R.color.my_primary_light);
                        return true;
                    case MotionEvent.ACTION_UP:
                        setBgColor(toItemVG, R.color.my_primary);
                        navSettingsVP.setVisibility(View.INVISIBLE);
                        navSettingsToVP.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
        //        to layout
        //clear button
        ImageButton toLayoutClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_to_clear_btn);
        toLayoutClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.VISIBLE);
                navSettingsToVP.setVisibility(View.INVISIBLE);
            }
        });
        //  to layout: items
        initUseCurrentLocationHandler(false);
        initPointOnMapHandler(false);
        initSearchLocationHandler(false, true);
        initSearchLocationHandler(false, false);
    }

    /**
     * add end point marker to map
     *
     * @param endPoint
     */
    private void addToMarker(GeoPoint endPoint) {
        MapHandler.getMapHandler().setStartEndPoint(endPoint, false, true);
    }

    /**
     * add start point marker to map
     *
     * @param startPoint
     */
    private void addFromMarker(GeoPoint startPoint) {
        MapHandler.getMapHandler().setStartEndPoint(startPoint, true, true);
    }

    /**
     * settings layout:
     * <p>
     * from item handler: when from item is clicked
     */
    private void initSettingsFromItemHandler() {
        final ViewGroup fromFieldVG = (ViewGroup) activity.findViewById(R.id.map_nav_settings_from_item);
        fromFieldVG.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBgColor(fromFieldVG, R.color.my_primary_light);
                        return true;
                    case MotionEvent.ACTION_UP:
                        setBgColor(fromFieldVG, R.color.my_primary);
                        navSettingsVP.setVisibility(View.INVISIBLE);
                        navSettingsFromVP.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
        ImageButton fromLayoutClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_from_clear_btn);
        fromLayoutClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.VISIBLE);
                navSettingsFromVP.setVisibility(View.INVISIBLE);
            }
        });
        initUseCurrentLocationHandler(true);
        initPointOnMapHandler(true);
        initSearchLocationHandler(true, true);
        initSearchLocationHandler(true, false);
    }

    /**
     * Point item view group
     * <p>
     * preform actions when point on map item is clicked
     */
    private void initPointOnMapHandler(final boolean isStartP) {
      int viewID = R.id.map_nav_settings_to_point;
      if (isStartP) { viewID = R.id.map_nav_settings_from_point; }
        final ViewGroup pointItem = (ViewGroup) activity.findViewById(viewID);
        pointItem.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBgColor(pointItem, R.color.my_primary_light);
                        return true;
                    case MotionEvent.ACTION_UP:
                        setBgColor(pointItem, R.color.my_primary);
                        if (isStartP)
                        { //touch on map
                          tabAction = TabAction.StartPoint;
                          navSettingsFromVP.setVisibility(View.INVISIBLE);
                          Toast.makeText(activity, "Touch on Map to choose your start Location",
                            Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                          tabAction = TabAction.EndPoint;
                          navSettingsToVP.setVisibility(View.INVISIBLE);
                          Toast.makeText(activity, "Touch on Map to choose your destination Location",
                            Toast.LENGTH_SHORT).show();
                        }
                        MapHandler.getMapHandler().setNeedLocation(true);
                        return true;
                }
                return false;
            }
        });
    }
    

    private void initSearchLocationHandler(final boolean isStartP, final boolean fromFavourite) {
      int viewID = R.id.map_nav_settings_to_search;
      if (isStartP) { viewID = R.id.map_nav_settings_from_search; }
      if (fromFavourite)
      {
        viewID = R.id.map_nav_settings_to_favorite;
        if (isStartP) { viewID = R.id.map_nav_settings_from_favorite; }
      }
      final ViewGroup pointItem = (ViewGroup) activity.findViewById(viewID);
      pointItem.setOnTouchListener(new View.OnTouchListener() {
          @Override public boolean onTouch(View v, MotionEvent event) {
              switch (event.getAction()) {
                  case MotionEvent.ACTION_DOWN:
                      setBgColor(pointItem, R.color.my_primary_light);
                      return true;
                  case MotionEvent.ACTION_UP:
                      setBgColor(pointItem, R.color.my_primary);
                      Intent intent = new Intent(activity, GeocodeActivity.class);
                      OnClickAddressListener callbackListener = new OnClickAddressListener()
                      {
                        @Override
                        public void onClick(Address addr)
                        {
                          GeoPoint newPos = new GeoPoint(addr.getLatitude(), addr.getLongitude());
                          if (isStartP)
                          {
                            Destination.getDestination().setStartPoint(newPos);
                            fromLocalET.setText(Destination.getDestination().getStartPointToString());
                            addFromMarker(Destination.getDestination().getStartPoint());
                            navSettingsFromVP.setVisibility(View.INVISIBLE);
                          }
                          else
                          {
                            Destination.getDestination().setEndPoint(newPos);
                            toLocalET.setText(Destination.getDestination().getEndPointToString());
                            addToMarker(Destination.getDestination().getEndPoint());
                            navSettingsToVP.setVisibility(View.INVISIBLE);
                          }
                          boolean showingNavigator = activeNavigator();
                          if (!showingNavigator)
                          {
                            navSettingsVP.setVisibility(View.VISIBLE);
                          }
                          MapHandler.getMapHandler().centerPointOnMap(newPos, 0, 0, 0);
                        }
                      };
                      GeoPoint[] points = null;
                      if (fromFavourite)
                      {
                        points = new GeoPoint[3];
                        points[0] = Destination.getDestination().getStartPoint();
                        points[1] = Destination.getDestination().getEndPoint();
                        Location curLoc = MapActivity.getmCurrentLocation();
                        if (curLoc != null)
                        {
                          points[2] = new GeoPoint(curLoc.getLatitude(), curLoc.getLongitude());
                        }
                      }
                      GeocodeActivity.setPre(callbackListener, points);
                      activity.startActivity(intent);
                      return true;
              }
              return false;
          }
      });
  }

    /**
     * current location handler: preform actions when current location item is clicked
     */
    private void initUseCurrentLocationHandler(final boolean isStartP) {
        int viewID = R.id.map_nav_settings_to_current;
        if (isStartP) { viewID = R.id.map_nav_settings_from_current; }
        final ViewGroup useCurrentLocal = (ViewGroup) activity.findViewById(viewID);
        useCurrentLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBgColor(useCurrentLocal, R.color.my_primary_light);
                        return true;
                    case MotionEvent.ACTION_UP:
                        setBgColor(useCurrentLocal, R.color.my_primary);
                        if (MapActivity.getmCurrentLocation() != null) {
                            GeoPoint newPos = new GeoPoint(MapActivity.getmCurrentLocation().getLatitude(),
                                                  MapActivity.getmCurrentLocation().getLongitude());
                            if (isStartP)
                            {
                              Destination.getDestination().setStartPoint(newPos);
                              addFromMarker(Destination.getDestination().getStartPoint());
                              fromLocalET.setText(Destination.getDestination().getStartPointToString());
                              navSettingsFromVP.setVisibility(View.INVISIBLE);
                            }
                            else
                            {
                              Destination.getDestination().setEndPoint(newPos);
                              addToMarker(Destination.getDestination().getEndPoint());
                              toLocalET.setText(Destination.getDestination().getEndPointToString());
                              navSettingsToVP.setVisibility(View.INVISIBLE);
                            }
                            boolean showingNavigator = activeNavigator();
                            if (!showingNavigator)
                            {
                              navSettingsVP.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(activity, "Current Location not available, Check your GPS signal!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * when use press on the screen to get a location form map
     *
     * @param latLong
     */
    @Override public void onPressLocation(GeoPoint latLong) {
        if (tabAction == TabAction.StartPoint) {
            Destination.getDestination().setStartPoint(latLong);
            addFromMarker(latLong);
            fromLocalET.setText(Destination.getDestination().getStartPointToString());
        }
        else if (tabAction == TabAction.EndPoint)
        {
            Destination.getDestination().setEndPoint(latLong);
            addToMarker(latLong);
            toLocalET.setText(Destination.getDestination().getEndPointToString());
        }
        if (tabAction != TabAction.None)
        {
          boolean showingNavigator = activeNavigator();
          if (!showingNavigator)
          {
            navSettingsVP.setVisibility(View.VISIBLE);
          }
        }
        tabAction = TabAction.None;
    }

    /**
     * calculate path calculating (running) true NOT running or finished false
     *
     * @param shortestPathRunning
     */
    @Override public void pathCalculating(boolean calculatingPathActive) {
        if (!calculatingPathActive && Navigator.getNavigator().getGhResponse() != null) {
            if (!NaviEngine.getNaviEngine().isNavigating())
            {
              activeDirections();
            }
        }
    }

    /**
     * drawer polyline on map , active navigator instructions(directions) if on
     * @return True when pathfinder-routes will be shown.
     */
    private boolean activeNavigator() {
        GeoPoint startPoint = Destination.getDestination().getStartPoint();
        GeoPoint endPoint = Destination.getDestination().getEndPoint();
        if (startPoint != null && endPoint != null) {
            // show path finding process
            navSettingsVP.setVisibility(View.INVISIBLE);

            View pathfinding = activity.findViewById(R.id.map_nav_settings_path_finding);
            pathfinding.setVisibility(View.VISIBLE);
            MapHandler mapHandler = MapHandler.getMapHandler();
            if (Variable.getVariable().isDirectionsON()) {
                mapHandler.setNeedPathCal(true);
                //rest running at
            }
            return true;
        }
        return false;
    }

    /**
     * active directions, and directions view
     */
    private void activeDirections() {
        RecyclerView instructionsRV;
        RecyclerView.Adapter<?> instructionsAdapter;
        RecyclerView.LayoutManager instructionsLayoutManager;

        instructionsRV = (RecyclerView) activity.findViewById(R.id.nav_instruction_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        instructionsRV.setHasFixedSize(true);

        // use a linear layout manager
        instructionsLayoutManager = new LinearLayoutManager(activity);
        instructionsRV.setLayoutManager(instructionsLayoutManager);

        // specify an adapter (see also next example)
        instructionsAdapter = new InstructionAdapter(Navigator.getNavigator().getGhResponse().getInstructions());
        instructionsRV.setAdapter(instructionsAdapter);
        initNavListView();
    }

    /**
     * navigation list view
     * <p>
     * make nav list view control button ready to use
     */
    private void initNavListView() {
        fillNavListSummaryValues();
        navSettingsVP.setVisibility(View.INVISIBLE);
        navInstructionListVP.setVisibility(View.VISIBLE);
        ImageButton clearBtn, stopBtn, startNavBtn, stopNavBtn;
        stopBtn = (ImageButton) activity.findViewById(R.id.nav_instruction_list_stop_btn);
        clearBtn = (ImageButton) activity.findViewById(R.id.nav_instruction_list_clear_btn);
        startNavBtn = (ImageButton) activity.findViewById(R.id.nav_instruction_list_start_btn);
        stopNavBtn = (ImageButton) activity.findViewById(R.id.navtop_stop);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(R.string.stop_navigation_msg).setTitle(R.string.stop_navigation)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // stop!
                                Navigator.getNavigator().setOn(false);
                                //delete polyline and markers
                                removeNavigation();
                                navInstructionListVP.setVisibility(View.INVISIBLE);
                                navSettingsVP.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
                // Create the AlertDialog object and return it

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navInstructionListVP.setVisibility(View.INVISIBLE);
                navSettingsVP.setVisibility(View.INVISIBLE);
                sideBarVP.setVisibility(View.VISIBLE);
            }
        });

        startNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Navigator.getNavigator().setNaviStart(activity, true);
            }
        });

        stopNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Navigator.getNavigator().setNaviStart(activity, false);
            }
        });
    }

    /**
     * fill up values for nav list summary
     */
    private void fillNavListSummaryValues() {
        ImageView travelMode;
        travelMode = (ImageView) activity.findViewById(R.id.nav_instruction_list_travel_mode_iv);
        travelMode.setImageResource(Navigator.getNavigator().getTravelModeResId(true));
        TextView from, to, distance, time;
        from = (TextView) activity.findViewById(R.id.nav_instruction_list_summary_from_tv);
        to = (TextView) activity.findViewById(R.id.nav_instruction_list_summary_to_tv);
        distance = (TextView) activity.findViewById(R.id.nav_instruction_list_summary_distance_tv);
        time = (TextView) activity.findViewById(R.id.nav_instruction_list_summary_time_tv);

        from.setText(Destination.getDestination().getStartPointToString());
        to.setText(Destination.getDestination().getEndPointToString());
        distance.setText(Navigator.getNavigator().getDistance());
        time.setText(Navigator.getNavigator().getTime());
    }

    /**
     * remove polyline, markers from map layers
     * <p>
     * set from & to = null
     */
    private void removeNavigation() {
        MapHandler.getMapHandler().removeMarkers();
        fromLocalET.setText("");
        toLocalET.setText("");
        Navigator.getNavigator().setOn(false);
        Destination.getDestination().setStartPoint(null);
        Destination.getDestination().setEndPoint(null);
    }

    /**
     * set up travel mode
     */
    private void initTravelModeSetting() {
        final ImageButton footBtn, bikeBtn, carBtn;
        footBtn = (ImageButton) activity.findViewById(R.id.nav_settings_foot_btn);
        bikeBtn = (ImageButton) activity.findViewById(R.id.nav_settings_bike_btn);
        carBtn = (ImageButton) activity.findViewById(R.id.nav_settings_car_btn);
        // init travel mode
        switch (Variable.getVariable().getTravelMode()) {
            case "foot":
                footBtn.setImageResource(R.drawable.ic_directions_walk_orange_24dp);
                break;
            case "bike":
                bikeBtn.setImageResource(R.drawable.ic_directions_bike_orange_24dp);
                break;
            case "car":
                carBtn.setImageResource(R.drawable.ic_directions_car_orange_24dp);
                break;
        }

        //foot
        footBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!Variable.getVariable().getTravelMode().equalsIgnoreCase("foot")) {
                    Variable.getVariable().setTravelMode("foot");
                    footBtn.setImageResource(R.drawable.ic_directions_walk_orange_24dp);
                    bikeBtn.setImageResource(R.drawable.ic_directions_bike_white_24dp);
                    carBtn.setImageResource(R.drawable.ic_directions_car_white_24dp);
                    activeNavigator();
                }
            }
        });
        //bike
        bikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!Variable.getVariable().getTravelMode().equalsIgnoreCase("bike")) {
                    Variable.getVariable().setTravelMode("bike");
                    footBtn.setImageResource(R.drawable.ic_directions_walk_white_24dp);
                    bikeBtn.setImageResource(R.drawable.ic_directions_bike_orange_24dp);
                    carBtn.setImageResource(R.drawable.ic_directions_car_white_24dp);
                    activeNavigator();
                }
            }
        });
        // car
        carBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!Variable.getVariable().getTravelMode().equalsIgnoreCase("car")) {
                    Variable.getVariable().setTravelMode("car");
                    footBtn.setImageResource(R.drawable.ic_directions_walk_white_24dp);
                    bikeBtn.setImageResource(R.drawable.ic_directions_bike_white_24dp);
                    carBtn.setImageResource(R.drawable.ic_directions_car_orange_24dp);
                    activeNavigator();
                }
            }
        });
    }

    /**
     * handler clicks on nav button
     */
    private void initNavBtnHandler() {
        navigationBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                sideBarVP.setVisibility(View.INVISIBLE);
                if (Navigator.getNavigator().isOn()) {
                    navInstructionListVP.setVisibility(View.VISIBLE);
                } else {
                    navSettingsVP.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    /**
     * start button: control button handler FAB
     */

    private void initControlBtnHandler() {
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
                    controlBtn.setImageResource(R.drawable.ic_keyboard_arrow_down_white_24dp);
                    controlBtn.startAnimation(anim);
                }
            }
        });
    }

    /**
     * implement zoom btn
     */
    protected void initZoomControlHandler(final MapView mapView) {
        zoomInBtn.setImageResource(R.drawable.ic_add_white_24dp);
        zoomOutBtn.setImageResource(R.drawable.ic_remove_white_24dp);

        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mapView.map().getMapPosition().getZoomLevel() < Variable.getVariable().getZoomLevelMax()) { doZoom(mapView, true); }
            }
        });
        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mapView.map().getMapPosition().getZoomLevel() > Variable.getVariable().getZoomLevelMin()) { doZoom(mapView, false); }
            }
        });
    }
    
    void doZoom(MapView mapView, boolean doZoomIn)
    {
      MapPosition mvp = mapView.map().getMapPosition();
      int i = mvp.getZoomLevel();
      log("Zoom from " + mvp.getZoomLevel() + " scale=" + mvp.getScale());
      if (doZoomIn) { mvp.setZoomLevel(++i); mvp.setScale(mvp.getScale() * 1.1); /* roundoff err */ }
      else { mvp.setZoomLevel(--i); }
      log("Zoom to " + mvp.getZoomLevel());
      mapView.map().animator().animateTo(300, mvp);
    }

    /**
     * move map to my current location as the center of the screen
     */
    protected void initShowMyLocation(final MapView mapView) {
        showPositionBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (MapActivity.getmCurrentLocation() != null) {
                    showPositionBtn.setImageResource(R.drawable.ic_my_location_white_24dp);
                    MapHandler.getMapHandler().centerPointOnMap(
                            new GeoPoint(MapActivity.getmCurrentLocation().getLatitude(),
                                    MapActivity.getmCurrentLocation().getLongitude()), 0, 0, 0);

                    //                    mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
                    //                            new LatLong(MapActivity.getmCurrentLocation().getLatitude(),
                    //                                    MapActivity.getmCurrentLocation().getLongitude()),
                    //                            mapView.getModel().mapViewPosition.getZoomLevel()));

                } else {
                    showPositionBtn.setImageResource(R.drawable.ic_location_searching_white_24dp);
                    Toast.makeText(activity, "No Location Available", Toast.LENGTH_SHORT).show();
                }
                ((MapActivity)activity).ensureLocationListener(false);
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
    @Override public void onStatusChanged(boolean on) {
        if (on) {
            navigationBtn.setImageResource(R.drawable.ic_directions_white_24dp);
        } else {
            navigationBtn.setImageResource(R.drawable.ic_navigation_white_24dp);
        }
    }
    
    @Override public void onNaviStart(boolean on) {
      navInstructionListVP.setVisibility(View.INVISIBLE);
      navSettingsVP.setVisibility(View.INVISIBLE);
      if (on) {
          sideBarVP.setVisibility(View.INVISIBLE);
          navTopVP.setVisibility(View.VISIBLE);
      } else {
          sideBarVP.setVisibility(View.VISIBLE);
          navTopVP.setVisibility(View.INVISIBLE);
      }
    }

    /**
     * called from Map activity when onBackpressed
     *
     * @return false no actions will perform; return true MapActivity will be placed back in the activity stack
     */
    public boolean homeBackKeyPressed() {
        if (navSettingsVP.getVisibility() == View.VISIBLE) {
            navSettingsVP.setVisibility(View.INVISIBLE);
            sideBarVP.setVisibility(View.VISIBLE);
            return false;
        } else if (navSettingsFromVP.getVisibility() == View.VISIBLE) {
            navSettingsFromVP.setVisibility(View.INVISIBLE);
            navSettingsVP.setVisibility(View.VISIBLE);
            return false;
        } else if (navSettingsToVP.getVisibility() == View.VISIBLE) {
            navSettingsToVP.setVisibility(View.INVISIBLE);
            navSettingsVP.setVisibility(View.VISIBLE);
            return false;
        } else if (navInstructionListVP.getVisibility() == View.VISIBLE) {
            navInstructionListVP.setVisibility(View.INVISIBLE);
            sideBarVP.setVisibility(View.VISIBLE);
            return false;
        } else if (appSettings.getAppSettingsVP() != null &&
                appSettings.getAppSettingsVP().getVisibility() == View.VISIBLE) {
            appSettings.getAppSettingsVP().setVisibility(View.INVISIBLE);
            sideBarVP.setVisibility(View.VISIBLE);
            return false;
        } else if (NaviEngine.getNaviEngine().isNavigating()) {
            Navigator.getNavigator().setNaviStart(activity, false);
            return false;
        } else {
            return true;
        }
    }

    public AppSettings getAppSettings() { return appSettings; }

    private void log(String str) {
        Log.i(MapActions.class.getName(), str);
    }

}