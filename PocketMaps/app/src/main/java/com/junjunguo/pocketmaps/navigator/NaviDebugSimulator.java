package com.junjunguo.pocketmaps.navigator;

import java.util.ArrayList;

import org.oscim.core.GeoPoint;

import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.junjunguo.pocketmaps.activities.MapActivity;
import com.junjunguo.pocketmaps.map.Tracking;

import android.app.Activity;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

public class NaviDebugSimulator
{
  private static final boolean DEBUG_SIMULATOR = false;
  private static boolean debug_simulator_run = false;
  private static boolean debug_simulator_from_tracking = false;
  private static ArrayList<GeoPoint> debug_simulator_points;
  private static NaviDebugSimulator instance;
  
  public static NaviDebugSimulator getSimu()
  {
    if (instance == null) { instance = new NaviDebugSimulator(); }
    return instance;
  }
  
  public void setSimuRun(boolean run) { debug_simulator_run = run; }
  
  /** The DEBUG_SIMULATOR will simulate first generated route on naviStart and trackingStart. **/
  public void startDebugSimulator(final Activity activity, InstructionList instructions, boolean fromTracking)
  {
    if (!DEBUG_SIMULATOR) { return; }
    debug_simulator_from_tracking = fromTracking;
    if (instructions == null) { return; }
    if (debug_simulator_points == null)
    {
      debug_simulator_points = new ArrayList<GeoPoint>();
      for (Instruction ins : instructions)
      {
        for (int i=0; i<ins.getPoints().size(); i++)
        {
          debug_simulator_points.add(new GeoPoint(ins.getPoints().getLat(i),ins.getPoints().getLon(i)));
        }
      }
    }
    final Location pLoc = new Location((String)null);
    final Location lastLoc = new Location((String)null);
    debug_simulator_run = true;
    runDelayed(activity, pLoc, lastLoc, 0);
  }
  
  private void runDelayed(final Activity activity, final Location pLoc, final Location lastLoc, final int index)
  {
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable()
    {
      @Override
      public void run()
      {
        if (debug_simulator_from_tracking &&
            !Tracking.getTracking().isTracking()) { debug_simulator_run = false; }
        if (!debug_simulator_run) { return; }
        GeoPoint p = debug_simulator_points.get(index);
        final MapActivity cur = ((MapActivity)activity);
        pLoc.setLatitude(p.getLatitude());
        pLoc.setLongitude(p.getLongitude());
        float bearing = lastLoc.bearingTo(pLoc);
        lastLoc.set(pLoc);
        pLoc.setBearing(bearing);
        pLoc.setSpeed(5.55f);
        cur.onLocationChanged(pLoc);
        log("Update position for Debug purpose! Lat=" + pLoc.getLatitude() + " Lon=" + pLoc.getLongitude());
        int newIndex = index+1;
        if (debug_simulator_points.size() > newIndex)
        {
          runDelayed(activity, pLoc, lastLoc, newIndex);
        }
      }
    }, 3000);
  }

  private void log(String str)
  {
    Log.i(NaviDebugSimulator.class.getName(), str);
  }
}
