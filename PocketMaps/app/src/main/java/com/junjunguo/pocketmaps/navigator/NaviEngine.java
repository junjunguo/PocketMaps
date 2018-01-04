package com.junjunguo.pocketmaps.navigator;

import org.oscim.core.GeoPoint;

import com.graphhopper.android.GHAsyncTask;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.map.MapHandler;
import com.junjunguo.pocketmaps.map.Navigator;
import com.junjunguo.pocketmaps.util.GeoMath;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class NaviEngine
{
  /** The DEBUG_SIMULATOR will simulate first generated route on naviStart and trackingStart. **/
  private static final double MAX_WAY_TOLERANCE = GeoMath.DEGREE_PER_METER * 50.0;
  private static final double MAX_WAY_TOLERANCE_METER = 50.0;

  private static final int BEST_NAVI_ZOOM = 18;
  enum UiJob { Nothing, RecalcPath, UpdateInstruction, Finished };
  private UiJob uiJob = UiJob.Nothing;
  NaviVoice naviVoice;
  boolean naviVoiceSpoken = false;
  private GeoPoint recalcFrom, recalcTo;
  private static NaviEngine instance;
  private Location pos;
  private boolean active = false;
  private InstructionList instructions;
  private ImageView navtop_image;
  private TextView navtop_curloc;
  private TextView navtop_nextloc;
  private TextView navtop_when;
  private TextView navtop_time;
  GHAsyncTask<GeoPoint, NaviInstruction, NaviInstruction> naviEngineTask;
  private int curPointPos = 0; // Array position.
  private double curPointDist = Double.MAX_VALUE;
  private double partDistanceScaler = 1.0;

  public static NaviEngine getNaviEngine()
  {
    if (instance == null) { instance = new NaviEngine(); }
    return instance;
  }
  
  public boolean isNavigating()
  {
    return active;
  }
  
  public void setNavigating(Activity activity, boolean active)
  {
    this.active = active;
    if (naviVoice == null)
    {
      naviVoice = new NaviVoice(activity.getApplicationContext());
    }
    if (active == false)
    {
      if (pos != null)
      {
        GeoPoint curPos = new GeoPoint(pos.getLatitude(), pos.getLongitude());
        MapHandler.getMapHandler().centerPointOnMap(curPos, BEST_NAVI_ZOOM, 0, 0);
      }
      NaviDebugSimulator.getSimu().setSimuRun(false);
      return;
    }
    naviVoiceSpoken = false;
    uiJob = UiJob.Nothing;
    initFields(activity);
    instructions = Navigator.getNavigator().getGhResponse().getInstructions();
    if (instructions.size() > 0)
    {
      startDebugSimulator(activity, false);
    }
  }
  
  public void onUpdateInstructions(InstructionList instructions)
  {
    if (uiJob != UiJob.RecalcPath) { throw new IllegalStateException("Getting instructions but state is not RecalcPath!"); }
    this.instructions = instructions;
    getNewInstruction();
    uiJob = UiJob.UpdateInstruction;
  }
  
  public void startDebugSimulator(Activity activity, boolean fromTracking)
  {
    NaviDebugSimulator.getSimu().startDebugSimulator(activity, instructions, fromTracking);
  }

  private GeoPoint findClosestStreet(GeoPoint fromPos)
  {
    QueryResult pos = MapHandler.getMapHandler().getHopper().getLocationIndex().findClosest(fromPos.getLatitude(), fromPos.getLongitude(), EdgeFilter.ALL_EDGES);
    int n = pos.getClosestEdge().getBaseNode();
    NodeAccess nodeAccess = MapHandler.getMapHandler().getHopper().getGraphHopperStorage().getNodeAccess();
    GeoPoint gp = new GeoPoint(nodeAccess.getLat(n), nodeAccess.getLon(n));
    return gp;
  }
  
  private void initFields(Activity activity)
  {
    if (navtop_image != null) { return; }
    navtop_image = activity.findViewById(R.id.navtop_image);
    navtop_curloc = activity.findViewById(R.id.navtop_curloc);
    navtop_nextloc = activity.findViewById(R.id.navtop_nextloc);
    navtop_when = activity.findViewById(R.id.navtop_when);
    navtop_time = activity.findViewById(R.id.navtop_time);
  }
  
  public void updatePosition(Activity activity, Location pos)
  {
    if (active == false) { return; }
    if (this.pos == null) { this.pos = new Location((String)null); }
    this.pos.set(pos);
    GeoPoint curPos = new GeoPoint(pos.getLatitude(), pos.getLongitude());
    GeoPoint newCenter = curPos.destinationPoint(70.0, pos.getBearing());
    MapHandler.getMapHandler().centerPointOnMap(newCenter, BEST_NAVI_ZOOM, 360.0f - pos.getBearing(), 45.0f);
    
    calculatePositionAsync(curPos);
  }
  
  private void calculatePositionAsync(GeoPoint curPos)
  {
    if (naviEngineTask == null) { createNaviEngineTask(); }
    else if (naviEngineTask.getStatus() == Status.RUNNING)
    {
      log("Error, NaviEngineTask is still running! Drop job ...");
    }
    else if (naviEngineTask.hasError())
    {
      naviEngineTask.getError().printStackTrace();
    }
    else
    {
      createNaviEngineTask(); //TODO: Asynctask kann nur einmal gestartet werden.
      naviEngineTask.execute(curPos);
    }
  }
  
  private void createNaviEngineTask()
  {
    naviEngineTask = new GHAsyncTask<GeoPoint, NaviInstruction, NaviInstruction>()
    {
      @Override
      protected NaviInstruction saveDoInBackground(GeoPoint... params) throws Exception
      {
        return calculatePosition(params[0]);
      }
      
      @Override
      protected void onPostExecute(NaviInstruction in)
      {
        if (in == null)
        {
          if (NaviEngine.this.uiJob == UiJob.RecalcPath)
          {
            if (instructions != null)
            {
              instructions = null;
              MapHandler.getMapHandler().calcPath(recalcFrom.getLatitude(), recalcFrom.getLongitude(), recalcTo.getLatitude(), recalcTo.getLongitude());
              log("Recalculating of path!!!");
            }
          }
          else if (NaviEngine.this.uiJob == UiJob.Finished)
          {
            active = false;
          }
        }
        else if (NaviEngine.this.uiJob == UiJob.UpdateInstruction)
        {
          navtop_when.setText(in.getNextDistanceString());
          navtop_time.setText(in.getFullTimeString());
          navtop_curloc.setText(in.getCurStreet());
          navtop_nextloc.setText(in.getNextInstruction());
          navtop_image.setImageResource(in.getNextSignResource());
        }
      }
    };
  }
  
  /** Worker Task **/
  private NaviInstruction calculatePosition(GeoPoint curPos)
  {
    if (uiJob == UiJob.RecalcPath) { return null; }
    if (uiJob == UiJob.Finished) { return null; }
log("NaviTask step 1 status=" + uiJob);
    int nextPointPos = curPointPos;
    int nearestPointPos = curPointPos;
    double nearestDist = Double.MAX_VALUE;
    while (instructions.get(0).getPoints().size() > nextPointPos)
    {
      double lat = instructions.get(0).getPoints().getLatitude(nextPointPos);
      double lon = instructions.get(0).getPoints().getLongitude(nextPointPos);
      double dist = GeoMath.fastDistance(curPos.getLatitude(), curPos.getLongitude(), lat, lon);
      if (dist < nearestDist)
      {
        nearestDist = dist;
        nearestPointPos = nextPointPos;
      }
      nextPointPos++;
    }
log("NaviTask step 2");
    if (nearestPointPos > 0)
    { // Check dist to line
      double lat1 = instructions.get(0).getPoints().getLatitude(nearestPointPos);
      double lon1 = instructions.get(0).getPoints().getLongitude(nearestPointPos);
      double lat2 = instructions.get(0).getPoints().getLatitude(nearestPointPos-1);
      double lon2 = instructions.get(0).getPoints().getLongitude(nearestPointPos-1);
      double lDist = GeoMath.distToLineSegment(curPos.getLatitude(), curPos.getLongitude(), lat1, lon1, lat2, lon2);
      if (lDist < nearestDist) { nearestDist = lDist; }
    }
    if (nearestPointPos < instructions.get(0).getPoints().size()-1)
    { // Check dist to line
      double lat1 = instructions.get(0).getPoints().getLatitude(nearestPointPos);
      double lon1 = instructions.get(0).getPoints().getLongitude(nearestPointPos);
      double lat2 = instructions.get(0).getPoints().getLatitude(nearestPointPos+1);
      double lon2 = instructions.get(0).getPoints().getLongitude(nearestPointPos+1);
      double lDist = GeoMath.distToLineSegment(curPos.getLatitude(), curPos.getLongitude(), lat1, lon1, lat2, lon2);
      if (lDist < nearestDist) { nearestDist = lDist; }
    }
log("NaviTask step 3");
    if (nearestPointPos == curPointPos || nearestDist > MAX_WAY_TOLERANCE)
    {
log("NaviTask step 3x Check recalc");
      if (curPointDist < nearestDist || nearestDist > MAX_WAY_TOLERANCE)
      {
log("NaviTask step 3x Check seems recalc1");
        Instruction nearest = instructions.find(curPos.getLatitude(), curPos.getLongitude(), MAX_WAY_TOLERANCE_METER);
log("NaviTask step 3x Check seems recalc2");
        if (nearest == null)
        {
log("NaviTask step 3x Check seems recalc3 Research Instruction!");
          GeoPoint closestP = findClosestStreet(curPos);
          nearest = instructions.find(closestP.getLatitude(), closestP.getLongitude(), MAX_WAY_TOLERANCE_METER);
        }
log("NaviTask step 3x Check seems recalc4");
        if (nearest == null)
        {
log("NaviTask step 3x Start recalc");
          uiJob = UiJob.RecalcPath;
          recalcFrom = curPos;
          Instruction lastInstruction = instructions.get(instructions.size()-1);
          int lastPoint = lastInstruction.getPoints().size()-1;
          double lastPointLat = lastInstruction.getPoints().getLat(lastPoint);
          double lastPointLon = lastInstruction.getPoints().getLon(lastPoint);
          recalcTo = new GeoPoint(lastPointLat, lastPointLon);
          return null;
        }
        else
        { // Forward to nearest instruction.
log("NaviTask step 3x Forwarding to nearest instruction");
          int index = instructions.indexOf(nearest);
          for (int i = 0; i<index; i++)
          {
            instructions.remove(0);
          }
          return getNewInstruction();
        }
      }
      else
      {
        //TODO: Eventuell nur Distanz pathPoint to curPoint aktualisieren.
        return getUpdatedInstruction(curPos, nearestPointPos, nearestDist);
      }
    }
    else if (nearestPointPos == instructions.get(0).getPoints().size()-1)
    {
      instructions.remove(0);
      return getNewInstruction();
    }
    else
    {
      return getUpdatedInstruction(curPos, nearestPointPos, nearestDist);
    }
  }

  private NaviInstruction getNewInstruction()
  {
    curPointPos = 0;
    curPointDist = Double.MAX_VALUE;
    uiJob = UiJob.UpdateInstruction;
    if (instructions.size() > 0)
    {
      Instruction in = instructions.get(0);
      long fullTime = countFullTime(in.getTime());
      GeoPoint curPos = new GeoPoint(in.getPoints().getLat(0), in.getPoints().getLon(0));
      double partDistance = countPartDistance(curPos, in, 0);
      if (partDistance == 0) { partDistanceScaler = 1; }
      else
      {
        partDistanceScaler = in.getDistance() / partDistance;
      }
      Instruction nextIn = null;
      if (instructions.size() > 1) { nextIn = instructions.get(1); }
      NaviInstruction nIn = new NaviInstruction(in, nextIn, fullTime);
log("NaviTask step 3b exec new Instruction");
      if (speakDistanceCheck(in.getDistance()))
      {
        naviVoice.speak(nIn.getVoiceText());
        naviVoiceSpoken = true;
      }
      else
      {
        naviVoiceSpoken = false;
      }
      return nIn;
    }
log("NaviTask step 3b exit (null)");
    uiJob = UiJob.Finished;
    return null;
  }
  
  private NaviInstruction getUpdatedInstruction(GeoPoint curPos, int nearestPointPos, double nearestDist)
  {
    curPointPos = 0;
    curPointDist = Double.MAX_VALUE;
    uiJob = UiJob.UpdateInstruction;
    if (instructions.size() > 0)
    {
      Instruction in = instructions.get(0);
      long partTime = 0;
      double partDistance = countPartDistance(curPos, in, nearestPointPos);
      partDistance = partDistance * partDistanceScaler;
      if (in.getDistance() <= partDistance)
      {
        partDistance = in.getDistance();
        partTime = in.getTime();
      }
      else
      {
        double partValue = partDistance / in.getDistance();
        partTime = (long)(in.getTime() * partValue);
      }
      long fullTime = countFullTime(partTime);
      Instruction nextIn = null;
      if (instructions.size() > 1) { nextIn = instructions.get(1); }
      NaviInstruction newIn = new NaviInstruction(in, nextIn, fullTime);
      newIn.updateDist(partDistance);
      if (!naviVoiceSpoken && speakDistanceCheck(partDistance))
      {
        naviVoice.speak(newIn.getVoiceText());
        naviVoiceSpoken = true;
      }
log("NaviTask step 3c exec update Instruction");
      return newIn;
    }
log("NaviTask step 3c exit (null)");
    return null;
  }
  
  private boolean speakDistanceCheck(double dist)
  {
    if (dist < 500) { return true; }
log("DistanceCheck: curSpeed=" + pos.getSpeed() + " swSpeed=" + (100 * GeoMath.KMH_TO_MSEC));
    if (pos.getSpeed() > 100 * GeoMath.KMH_TO_MSEC)
    {
      if (dist < 900) { return true; }
    }
    return false;
  }

  /** Count all time of Instructions.
   * @param partTime Time of current Instruction. **/
  private long countFullTime(long partTime)
  {
    long fullTime = partTime;
    for (int i=1; i<instructions.size(); i++)
    {
      fullTime += instructions.get(i).getTime();
    }
    return fullTime;
  }
  
  /** Counts the estimated rest-distance to next instruction. **/
  private double countPartDistance(GeoPoint curPos, Instruction in, int nearestPointPos)
  {
    double partDistance = 0;
    double lastLat = curPos.getLatitude();
    double lastLon = curPos.getLongitude();
    for (int i=nearestPointPos+1; i<in.getPoints().size(); i++)
    {
      double nextLat = in.getPoints().getLat(i);
      double nextLon = in.getPoints().getLon(i);
      partDistance += GeoMath.fastDistance(lastLat, lastLon, nextLat, nextLon);
      lastLat = nextLat;
      lastLon = nextLon;
    }
    partDistance = partDistance * GeoMath.METER_PER_DEGREE;
    return partDistance;
  }

  private void log(String str)
  {
    Log.i(NaviEngine.class.getName(), str);
  }
}
