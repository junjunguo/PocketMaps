package com.junjunguo.pocketmaps.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Location;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.fragments.AppSettings;
import com.junjunguo.pocketmaps.fragments.AppSettings.SettType;
import com.junjunguo.pocketmaps.map.Destination;
import com.junjunguo.pocketmaps.model.SportCategory;
import com.junjunguo.pocketmaps.model.listeners.MapHandlerListener;
import com.junjunguo.pocketmaps.model.listeners.NavigatorListener;
import com.junjunguo.pocketmaps.model.listeners.OnClickAddressListener;
import com.junjunguo.pocketmaps.navigator.NaviEngine;
import com.junjunguo.pocketmaps.map.MapHandler;
import com.junjunguo.pocketmaps.map.Navigator;
import com.junjunguo.pocketmaps.fragments.InstructionAdapter;
import com.junjunguo.pocketmaps.fragments.SpinnerAdapter;
import com.junjunguo.pocketmaps.util.Calorie;
import com.junjunguo.pocketmaps.util.Variable;

import java.util.ArrayList;

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
    public final static String EMPTY_LOC_STR = "..........";
    enum TabAction{ StartPoint, EndPoint, AddFavourit, None };
    private TabAction tabAction = TabAction.None;
    private Activity activity;
    private AppSettings appSettings;
    protected FloatingActionButton showPositionBtn, navigationBtn, settingsBtn, settingsSetBtn, settingsNavBtn, controlBtn, favourBtn;
    protected FloatingActionButton zoomInBtn, zoomOutBtn;
    private ViewGroup sideBarVP, sideBarMenuVP, southBarSettVP, southBarFavourVP, navSettingsVP, navSettingsFromVP, navSettingsToVP,
            navInstructionListVP, navTopVP;
    private boolean menuVisible;
    private TextView fromLocalET, toLocalET;

    public MapActions(Activity activity, MapView mapView) {
        this.activity = activity;
        this.showPositionBtn = (FloatingActionButton) activity.findViewById(R.id.map_show_my_position_fab);
        this.navigationBtn = (FloatingActionButton) activity.findViewById(R.id.map_nav_fab);
        this.settingsBtn = (FloatingActionButton) activity.findViewById(R.id.map_southbar_settings_fab);
        this.settingsSetBtn = (FloatingActionButton) activity.findViewById(R.id.map_southbar_sett_sett_fab);
        this.settingsNavBtn = (FloatingActionButton) activity.findViewById(R.id.map_southbar_sett_nav_fab);
        this.favourBtn = (FloatingActionButton) activity.findViewById(R.id.map_southbar_favour_fab);
        this.controlBtn = (FloatingActionButton) activity.findViewById(R.id.map_sidebar_control_fab);
        this.zoomInBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_in_fab);
        this.zoomOutBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_out_fab);
        // view groups managed by separate layout xml file : //map_sidebar_layout/map_sidebar_menu_layout
        this.sideBarVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_layout);
        this.sideBarMenuVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_menu_layout);
        this.southBarSettVP = (ViewGroup) activity.findViewById(R.id.map_southbar_sett_layout);
        this.southBarFavourVP = (ViewGroup) activity.findViewById(R.id.map_southbar_favour_layout);
        this.navSettingsVP = (ViewGroup) activity.findViewById(R.id.nav_settings_layout);
        this.navTopVP = (ViewGroup) activity.findViewById(R.id.navtop_layout);
        this.navSettingsFromVP = (ViewGroup) activity.findViewById(R.id.nav_settings_from_layout);
        this.navSettingsToVP = (ViewGroup) activity.findViewById(R.id.nav_settings_to_layout);
        this.navInstructionListVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_list_layout);
        //form location and to location textView
        this.fromLocalET = (TextView) activity.findViewById(R.id.nav_settings_from_local_et);
        this.toLocalET = (TextView) activity.findViewById(R.id.nav_settings_to_local_et);
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
        initFavourBtnHandler();
        mapView.map().getEventLayer().enableRotation(false);
    }

    /**
     * init and implement performance for settings
     */
    private void initSettingsBtnHandler() {
        settingsSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                appSettings.showAppSettings(sideBarVP, SettType.Default);
            }
        });
        settingsNavBtn.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
              appSettings.showAppSettings(sideBarVP, SettType.Navi);
          }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            ColorStateList oriColor;
            @Override public void onClick(View v) {
              if (southBarSettVP.getVisibility() == View.VISIBLE)
              {
                settingsBtn.setBackgroundTintList(oriColor);
                southBarSettVP.setVisibility(View.INVISIBLE);
                favourBtn.setVisibility(View.VISIBLE);
                sideBarMenuVP.setVisibility(View.VISIBLE);
                controlBtn.setVisibility(View.VISIBLE);
              }
              else
              {
                oriColor = settingsBtn.getBackgroundTintList();
                settingsBtn.setBackgroundTintList(ColorStateList.valueOf(R.color.abc_color_highlight_material));
                southBarSettVP.setVisibility(View.VISIBLE);
                favourBtn.setVisibility(View.INVISIBLE);
                sideBarMenuVP.setVisibility(View.INVISIBLE);
                controlBtn.clearAnimation();
                controlBtn.setVisibility(View.INVISIBLE);
              }
            }
        });
    }
    
    /**
     * init and implement performance for favourites
     */
    private void initFavourBtnHandler() {
        initSearchLocationHandler(false, true, R.id.map_southbar_favour_add_fab, true);
        initPointOnMapHandler(TabAction.AddFavourit, R.id.map_southbar_favour_select_fab, true);

        favourBtn.setOnClickListener(new View.OnClickListener() {
            ColorStateList oriColor;
            @Override public void onClick(View v) {
              if (southBarFavourVP.getVisibility() == View.VISIBLE)
              {
                favourBtn.setBackgroundTintList(oriColor);
                southBarFavourVP.setVisibility(View.INVISIBLE);
                settingsBtn.setVisibility(View.VISIBLE);
                sideBarMenuVP.setVisibility(View.VISIBLE);
                controlBtn.setVisibility(View.VISIBLE);
              }
              else
              {
                oriColor = favourBtn.getBackgroundTintList();
                favourBtn.setBackgroundTintList(ColorStateList.valueOf(R.color.abc_color_highlight_material));
                southBarFavourVP.setVisibility(View.VISIBLE);
                settingsBtn.setVisibility(View.INVISIBLE);
                sideBarMenuVP.setVisibility(View.INVISIBLE);
                controlBtn.clearAnimation();
                controlBtn.setVisibility(View.INVISIBLE);
              }
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
        initTravelModeSetting();
        initSettingsFromItemHandler();
        initSettingsToItemHandler();
    }

    @SuppressWarnings("deprecation")
    private void setBgColor(View v, int color)
    {
      v.setBackgroundColor(activity.getResources().getColor(color));
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
        initUseCurrentLocationHandler(false, R.id.map_nav_settings_to_current, true);
        initPointOnMapHandler(TabAction.EndPoint, R.id.map_nav_settings_to_point, true);
        initPointOnMapHandler(TabAction.EndPoint, R.id.nav_settings_to_sel_btn, false);
        initEnterLatLonHandler(false, R.id.map_nav_settings_to_latlon);
        initClearCurrentLocationHandler(false, R.id.nav_settings_to_del_btn);
        initSearchLocationHandler(false, true, R.id.map_nav_settings_to_favorite, true);
        initSearchLocationHandler(false, false, R.id.map_nav_settings_to_search, true);
        initSearchLocationHandler(false, true, R.id.nav_settings_to_fav_btn, false);
        initSearchLocationHandler(false, false, R.id.nav_settings_to_search_btn, false);
    }

    /**
     * add end point marker to map
     *
     * @param endPoint
     */
    private void addToMarker(GeoPoint endPoint, boolean recalculate) {
        MapHandler.getMapHandler().setStartEndPoint(activity, endPoint, false, recalculate);
    }

    /**
     * add start point marker to map
     *
     * @param startPoint
     */
    private void addFromMarker(GeoPoint startPoint, boolean recalculate) {
        MapHandler.getMapHandler().setStartEndPoint(activity, startPoint, true, recalculate);
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
        initUseCurrentLocationHandler(true, R.id.map_nav_settings_from_current, true);
        initUseCurrentLocationHandler(true, R.id.nav_settings_from_cur_btn, false);
        initEnterLatLonHandler(true, R.id.map_nav_settings_from_latlon);
        initClearCurrentLocationHandler(true, R.id.nav_settings_from_del_btn);
        initPointOnMapHandler(TabAction.StartPoint, R.id.map_nav_settings_from_point, true);
        initSearchLocationHandler(true, true, R.id.map_nav_settings_from_favorite, true);
        initSearchLocationHandler(true, false, R.id.map_nav_settings_from_search, true);
        initSearchLocationHandler(true, true, R.id.nav_settings_from_fav_btn, false);
        initSearchLocationHandler(true, false, R.id.nav_settings_from_search_btn, false);
    }

    /**
     * Point item view group
     * <p>
     * preform actions when point on map item is clicked
     */
    private void initPointOnMapHandler(final TabAction tabType, int viewID, final boolean setBg) {
        final View pointItem = (View) activity.findViewById(viewID);
        pointItem.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (setBg) setBgColor(pointItem, R.color.my_primary_light);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (setBg) setBgColor(pointItem, R.color.my_primary);
                        if (tabType == TabAction.StartPoint)
                        { //touch on map
                          tabAction = TabAction.StartPoint;
                          navSettingsFromVP.setVisibility(View.INVISIBLE);
                          Toast.makeText(activity, "Touch on Map to choose your start Location",
                            Toast.LENGTH_SHORT).show();
                        }
                        else if (tabType == TabAction.EndPoint)
                        {
                          tabAction = TabAction.EndPoint;
                          navSettingsToVP.setVisibility(View.INVISIBLE);
                          Toast.makeText(activity, "Touch on Map to choose your destination Location",
                            Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                          tabAction = TabAction.AddFavourit;
                          sideBarVP.setVisibility(View.INVISIBLE);
                          Toast.makeText(activity, "Touch on Map to choose your destination Location",
                            Toast.LENGTH_SHORT).show();
                        }
                        navSettingsVP.setVisibility(View.INVISIBLE);
                        MapHandler.getMapHandler().setNeedLocation(true);
                        return true;
                }
                return false;
            }
        });
    }
    
    private void initEnterLatLonHandler(final boolean isStartP, int viewID) {
      final View pointItem = (View) activity.findViewById(viewID);
      pointItem.setOnTouchListener(new View.OnTouchListener() {
          @Override public boolean onTouch(View v, MotionEvent event) {
              switch (event.getAction()) {
                  case MotionEvent.ACTION_DOWN:
                      setBgColor(pointItem, R.color.my_primary_light);
                      return true;
                  case MotionEvent.ACTION_UP:
                      setBgColor(pointItem, R.color.my_primary);
                      Intent intent = new Intent(activity, LatLonActivity.class);
                      OnClickAddressListener callbackListener = createPosSelectedListener(isStartP);
                      LatLonActivity.setPre(callbackListener);
                      activity.startActivity(intent);
                      return true;
              }
              return false;
          }
      });
    }

    private void initSearchLocationHandler(final boolean isStartP, final boolean fromFavourite, int viewID, final boolean setBg) {
      final View pointItem = (View) activity.findViewById(viewID);
      pointItem.setOnTouchListener(new View.OnTouchListener() {
          @Override public boolean onTouch(View v, MotionEvent event) {
              switch (event.getAction()) {
                  case MotionEvent.ACTION_DOWN:
                      if (setBg) setBgColor(pointItem, R.color.my_primary_light);
                      return true;
                  case MotionEvent.ACTION_UP:
                      if (setBg) setBgColor(pointItem, R.color.my_primary);
                      GeoPoint[] points = null;
                      String names[] = null;
                      if (fromFavourite)
                      {
                        points = new GeoPoint[3];
                        points[0] = Destination.getDestination().getStartPoint();
                        points[1] = Destination.getDestination().getEndPoint();
                        names = new String[2];
                        names[0] = Destination.getDestination().getStartPointName();
                        names[1] = Destination.getDestination().getEndPointName();
                        Location curLoc = MapActivity.getmCurrentLocation();
                        if (curLoc != null)
                        {
                          points[2] = new GeoPoint(curLoc.getLatitude(), curLoc.getLongitude());
                        }
                      }
                      startGeocodeActivity(points, names, isStartP, false);
                      return true;
              }
              return false;
          }
      });
  }
    
    /** Shows the GeocodeActivity, or Favourites, if points are not null.
     *  @param points The points to add as favourites, [0]=start [1]=end [2]=cur. **/
    public void startGeocodeActivity(GeoPoint[] points, String[] names, boolean isStartP, boolean autoEdit)
    {
      Intent intent = new Intent(activity, GeocodeActivity.class);
      OnClickAddressListener callbackListener = createPosSelectedListener(isStartP);
      GeocodeActivity.setPre(callbackListener, points, names, autoEdit);
      activity.startActivity(intent);
    }

    private OnClickAddressListener createPosSelectedListener(final boolean isStartP)
    {
      OnClickAddressListener callbackListener = new OnClickAddressListener()
      {
        @Override
        public void onClick(Address addr)
        {
          GeoPoint newPos = new GeoPoint(addr.getLatitude(), addr.getLongitude());
          String fullAddress = "";
          for (int i=0; i<5; i++)
          {
            String curAddr = addr.getAddressLine(i);
            if (curAddr == null || curAddr.isEmpty()) { continue; }
            if (!fullAddress.isEmpty()) { fullAddress = fullAddress + ", "; }
            fullAddress = fullAddress + curAddr;
          }
          doSelectCurrentPos(newPos, fullAddress, isStartP);
        }
      };
      return callbackListener;
    }
    
    private void doSelectCurrentPos(GeoPoint newPos, String text, boolean isStartP)
    {
      if (isStartP)
      {
        Destination.getDestination().setStartPoint(newPos, text);
        fromLocalET.setText(text);
        addFromMarker(Destination.getDestination().getStartPoint(), true);
        navSettingsFromVP.setVisibility(View.INVISIBLE);
      }
      else
      {
        Destination.getDestination().setEndPoint(newPos, text);
        toLocalET.setText(text);
        addToMarker(Destination.getDestination().getEndPoint(), true);
        navSettingsToVP.setVisibility(View.INVISIBLE);
      }
      setQuickButtonsClearVisible(isStartP, true);
      sideBarVP.setVisibility(View.INVISIBLE);
      if (!activateNavigator())
      {
        navSettingsVP.setVisibility(View.VISIBLE);
      }
      MapHandler.getMapHandler().centerPointOnMap(newPos, 0, 0, 0);
    }

    /**
     * current location handler: preform actions when current location item is clicked
     */
    private void initUseCurrentLocationHandler(final boolean isStartP, int viewID, final boolean setBg) {
        final View useCurrentLocal = (View) activity.findViewById(viewID);
        useCurrentLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (setBg) setBgColor(useCurrentLocal, R.color.my_primary_light);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (setBg) setBgColor(useCurrentLocal, R.color.my_primary);
                        if (MapActivity.getmCurrentLocation() != null) {
                            GeoPoint newPos = new GeoPoint(MapActivity.getmCurrentLocation().getLatitude(),
                                                  MapActivity.getmCurrentLocation().getLongitude());
                            String text = "" + newPos.getLatitude() + ", " + newPos.getLongitude();
                            doSelectCurrentPos(newPos, text, isStartP);
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
     * current location handler: preform actions when clear location item is clicked
     */
    private void initClearCurrentLocationHandler(final boolean isStartP, int viewID) {
        final View useCurrentLocal = (View) activity.findViewById(viewID);
        useCurrentLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                            if (isStartP)
                            {
                              Destination.getDestination().setStartPoint(null, null);
                              addFromMarker(null, false);
                              fromLocalET.setText(EMPTY_LOC_STR);
                            }
                            else
                            {
                              Destination.getDestination().setEndPoint(null, null);
                              addToMarker(null, false);
                              toLocalET.setText(EMPTY_LOC_STR);
                            }
                            setQuickButtonsClearVisible(isStartP, false);
                        return true;
                }
                return false;
            }
        });
    }
    
    void setQuickButtonsClearVisible(boolean isStartP, boolean vis)
    {
      int curVis = View.VISIBLE;
      if (isStartP)
      {
        if (!vis) { curVis = View.INVISIBLE; }
        activity.findViewById(R.id.nav_settings_from_del_btn).setVisibility(curVis);
        if (vis) { curVis = View.INVISIBLE; }
        else { curVis = View.VISIBLE; }
        activity.findViewById(R.id.nav_settings_from_search_btn).setVisibility(curVis);
        activity.findViewById(R.id.nav_settings_from_fav_btn).setVisibility(curVis);
        activity.findViewById(R.id.nav_settings_from_cur_btn).setVisibility(curVis);
      }
      else
      {
        if (!vis) { curVis = View.INVISIBLE; }
        activity.findViewById(R.id.nav_settings_to_del_btn).setVisibility(curVis);
        if (vis) { curVis = View.INVISIBLE; }
        else { curVis = View.VISIBLE; }
        activity.findViewById(R.id.nav_settings_to_search_btn).setVisibility(curVis);
        activity.findViewById(R.id.nav_settings_to_fav_btn).setVisibility(curVis);
        activity.findViewById(R.id.nav_settings_to_sel_btn).setVisibility(curVis);
      }
    }
    
    /**
     * when use press on the screen to get a location form map
     *
     * @param latLong
     */
    @Override public void onPressLocation(GeoPoint latLong) {
        if (tabAction == TabAction.None) { return; }
        if (tabAction == TabAction.AddFavourit)
        {
          sideBarVP.setVisibility(View.VISIBLE);
          tabAction = TabAction.None;
          GeoPoint[] points = new GeoPoint[3];
          points[2] = latLong;
          String[] names = new String[3];
          names[2] = "Selected position";
          startGeocodeActivity(points, names, false, true);
          return;
        }
        String text = "" + latLong.getLatitude() + ", " + latLong.getLongitude();
        doSelectCurrentPos(latLong, text, tabAction == TabAction.StartPoint);
        tabAction = TabAction.None;
    }
    
    public void onPressLocationEndPoint(GeoPoint latLong)
    {
      tabAction = TabAction.EndPoint;
      onPressLocation(latLong);
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
              activateDirections();
            }
        }
    }

    /**
     * drawer polyline on map , active navigator instructions(directions) if on
     * @return True when pathfinder-routes will be shown.
     */
    private boolean activateNavigator() {
        GeoPoint startPoint = Destination.getDestination().getStartPoint();
        GeoPoint endPoint = Destination.getDestination().getEndPoint();
        if (startPoint != null && endPoint != null) {
            // show path finding process
            navSettingsVP.setVisibility(View.INVISIBLE);

            View pathfinding = activity.findViewById(R.id.map_nav_settings_path_finding);
            pathfinding.setVisibility(View.VISIBLE);
            if (Variable.getVariable().isDirectionsON()) {
                MapHandler.getMapHandler().setNeedPathCal(true);
                // Waiting for calculating
            }
            return true;
        }
        return false;
    }

    /**
     * active directions, and directions view
     */
    private void activateDirections() {
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
                Location curLoc = MapActivity.getmCurrentLocation();
                if (curLoc!=null)
                {
                  NaviEngine.getNaviEngine().updatePosition(activity, curLoc);
                }
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
        initSpinner();
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
    
    private void initSpinner() {
      Spinner spinner = (Spinner) activity.findViewById(R.id.nav_instruction_list_travel_mode_sp);

      ArrayList<SportCategory> spinnerList = new ArrayList<>();
      spinnerList.add(new SportCategory("walk", R.drawable.ic_directions_walk_orange_24dp, Calorie.Type.Run));
      spinnerList.add(new SportCategory("bike", R.drawable.ic_directions_bike_orange_24dp, Calorie.Type.Bike));
      spinnerList.add(new SportCategory("car", R.drawable.ic_directions_car_orange_24dp, Calorie.Type.Car));

      SpinnerAdapter adapter = new SpinnerAdapter(activity, R.layout.analytics_activity_type, spinnerList);
      // Specify the layout to use when the list of choices appears
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      // Apply the adapter to the spinner
      spinner.setAdapter(adapter);
      spinner.setSelection(Navigator.getNavigator().getTravelModeArrayIndex());
      spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          @Override public void onItemSelected(AdapterView<?> parentView, View v, int position, long id) {
              if (position == Navigator.getNavigator().getTravelModeArrayIndex()) { return; }
              Navigator.getNavigator().setTravelModeArrayIndex(position);
              navSettingsVP.setVisibility(View.VISIBLE);
              navInstructionListVP.setVisibility(View.INVISIBLE);
              MapHandler.getMapHandler().recalcPath(activity);
          }

          @Override public void onNothingSelected(AdapterView<?> parentView) {
          }
      });
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
        if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Foot)
        {
          footBtn.setImageResource(R.drawable.ic_directions_walk_orange_24dp);
        }
        else if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Bike)
        {
          bikeBtn.setImageResource(R.drawable.ic_directions_bike_orange_24dp);
        }
        else if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Car)
        {
          carBtn.setImageResource(R.drawable.ic_directions_car_orange_24dp);
        }

        //foot
        footBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                footBtn.setImageResource(R.drawable.ic_directions_walk_orange_24dp);
                bikeBtn.setImageResource(R.drawable.ic_directions_bike_white_24dp);
                carBtn.setImageResource(R.drawable.ic_directions_car_white_24dp);
                if (Variable.getVariable().getTravelMode() != Variable.TravelMode.Foot) {
                    Variable.getVariable().setTravelMode(Variable.TravelMode.Foot);
                    if (activateNavigator())
                    {
                      MapHandler.getMapHandler().recalcPath(activity);
                    }
                }
            }
        });
        //bike
        bikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                footBtn.setImageResource(R.drawable.ic_directions_walk_white_24dp);
                bikeBtn.setImageResource(R.drawable.ic_directions_bike_orange_24dp);
                carBtn.setImageResource(R.drawable.ic_directions_car_white_24dp);
                if (Variable.getVariable().getTravelMode() != Variable.TravelMode.Bike) {
                    Variable.getVariable().setTravelMode(Variable.TravelMode.Bike);
                    if (activateNavigator())
                    {
                      MapHandler.getMapHandler().recalcPath(activity);
                    }
                }
            }
        });
        // car
        carBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                footBtn.setImageResource(R.drawable.ic_directions_walk_white_24dp);
                bikeBtn.setImageResource(R.drawable.ic_directions_bike_white_24dp);
                carBtn.setImageResource(R.drawable.ic_directions_car_orange_24dp);
                if (Variable.getVariable().getTravelMode() != Variable.TravelMode.Car) {
                    Variable.getVariable().setTravelMode(Variable.TravelMode.Car);
                    if (activateNavigator())
                    {
                      MapHandler.getMapHandler().recalcPath(activity);
                    }
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
                    favourBtn.setVisibility(View.INVISIBLE);
                    settingsBtn.setVisibility(View.INVISIBLE);
                    controlBtn.setImageResource(R.drawable.ic_keyboard_arrow_up_white_24dp);
                    controlBtn.startAnimation(anim);
                } else {
                    setMenuVisible(true);
                    sideBarMenuVP.setVisibility(View.VISIBLE);
                    favourBtn.setVisibility(View.VISIBLE);
                    settingsBtn.setVisibility(View.VISIBLE);
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
        } else if (southBarSettVP.getVisibility() == View.VISIBLE) {
            settingsBtn.callOnClick();
            return false;
        } else if (southBarFavourVP.getVisibility() == View.VISIBLE) {
            favourBtn.callOnClick();
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