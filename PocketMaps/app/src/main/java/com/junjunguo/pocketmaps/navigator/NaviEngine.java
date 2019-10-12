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
import com.junjunguo.pocketmaps.util.LightSensor;
import com.junjunguo.pocketmaps.util.UnitCalculator;

import android.app.Activity;
import androidx.annotation.WorkerThread;
import androidx.annotation.UiThread;
import android.location.Location;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class NaviEngine
{
  /** The DEBUG_SIMULATOR will simulate first generated route on naviStart and trackingStart. **/
  private static final double MAX_WAY_TOLERANCE = GeoMath.DEGREE_PER_METER * 30.0;
  private static final double MAX_WAY_TOLERANCE_METER = 30.0;

  private static final int BEST_NAVI_ZOOM = 18;
  enum UiJob { Nothing, RecalcPath, UpdateInstruction, Finished };
  private UiJob uiJob = UiJob.Nothing;
  private boolean directTargetDir = false;
  NaviVoice naviVoice;
  LightSensor lightSensor;
  boolean naviVoiceSpoken = false;
  private GeoPoint recalcFrom, recalcTo;
  private static NaviEngine instance;
  private Location pos;
  final PointPosData nearestP = new PointPosData(); 
  private boolean active = false;
  private InstructionList instructions;
  private ImageView navtop_image;
  private TextView navtop_curloc;
  private TextView navtop_nextloc;
  private TextView navtop_when;
  private TextView navtop_time;
  private float tiltMult = 1.0f;
  private float tiltMultPos = 1.0f;
  GHAsyncTask<GeoPoint, NaviInstruction, NaviInstruction> naviEngineTask;
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
    if (active && lightSensor==null)
    {
      lightSensor = new LightSensor(activity);
    }
    else if ((!active) && lightSensor!=null)
    {
      lightSensor.cleanup(activity);
      lightSensor = null;
    }
    if (naviVoice == null)
    {
      naviVoice = new NaviVoice(activity.getApplicationContext());
    }
    if (active == false)
    {
      MapHandler.getMapHandler().setCustomPointIcon(activity, R.drawable.ic_my_location_dark_24dp);
      if (pos != null)
      {
        GeoPoint curPos = new GeoPoint(pos.getLatitude(), pos.getLongitude());
        MapHandler.getMapHandler().centerPointOnMap(curPos, BEST_NAVI_ZOOM, 0, 0);
      }
      NaviDebugSimulator.getSimu().setSimuRun(false);
      return;
    }
    MapHandler.getMapHandler().setCustomPointIcon(activity, R.drawable.ic_navigation_black_24dp);
    naviVoiceSpoken = false;
    uiJob = UiJob.Nothing;
    initFields(activity);
    instructions = Navigator.getNavigator().getGhResponse().getInstructions();
    resetNewInstruction();
    if (instructions.size() > 0)
    {
      startDebugSimulator(activity, false);
    }
  }
  
  /** This allows to update pathLayer, when instructions is just a line from target to curLoc.
   *  @param directTargetDir True to update pathLayer, join to curPos. **/
  public void setDirectTargetDir(boolean directTargetDir)
  {
    this.directTargetDir = directTargetDir;
  }
  
  public void onUpdateInstructions(InstructionList instructions)
  {
    if (uiJob != UiJob.RecalcPath) { throw new IllegalStateException("Getting instructions but state is not RecalcPath!"); }
    if (!directTargetDir)
    {
      nearestP.checkDirectionOk(pos, instructions.get(0), naviVoice);
    }
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
    if (MapHandler.getMapHandler().getHopper() == null) { return fromPos; } // Not loaded yet!
    QueryResult pos = MapHandler.getMapHandler().getHopper().getLocationIndex().findClosest(fromPos.getLatitude(), fromPos.getLongitude(), EdgeFilter.ALL_EDGES);
    int n = pos.getClosestEdge().getBaseNode();
    NodeAccess nodeAccess = MapHandler.getMapHandler().getHopper().getGraphHopperStorage().getNodeAccess();
    GeoPoint gp = new GeoPoint(nodeAccess.getLat(n), nodeAccess.getLon(n));
    return gp;
  }
  
  private void initFields(Activity activity)
  {
    navtop_image = activity.findViewById(R.id.navtop_image);
    navtop_curloc = activity.findViewById(R.id.navtop_curloc);
    navtop_nextloc = activity.findViewById(R.id.navtop_nextloc);
    navtop_when = activity.findViewById(R.id.navtop_when);
    navtop_time = activity.findViewById(R.id.navtop_time);
  }
  
  @UiThread
  public void updatePosition(Activity activity, Location pos)
  {
    if (active == false) { return; }
    if (uiJob == UiJob.RecalcPath) { return; }
    if (this.pos == null) { this.pos = new Location((String)null); }
    this.pos.set(pos);
    GeoPoint curPos = new GeoPoint(pos.getLatitude(), pos.getLongitude());
    GeoPoint newCenter = curPos.destinationPoint(70.0 * tiltMultPos, pos.getBearing());
    MapHandler.getMapHandler().centerPointOnMap(newCenter, BEST_NAVI_ZOOM, 360.0f - pos.getBearing(), 45.0f * tiltMult);
    
    calculatePositionAsync(activity, curPos);
  }

  @UiThread
  private void calculatePositionAsync(Activity activity, GeoPoint curPos)
  {
    if (naviEngineTask == null) { createNaviEngineTask(activity); }
    updateDirectTargetDir(curPos);
    if (naviEngineTask.getStatus() == Status.RUNNING)
    {
      log("Error, NaviEngineTask is still running! Drop job ...");
    }
    else if (naviEngineTask.hasError())
    {
      naviEngineTask.getError().printStackTrace();
    }
    else
    {
      createNaviEngineTask(activity); //TODO: Recreation of Asynctask seems necessary?!
      naviEngineTask.execute(curPos);
    }
  }
  
  private void updateDirectTargetDir(GeoPoint curPos)
  {
    if (!directTargetDir) { return; }
    MapHandler.getMapHandler().joinPathLayerToPos(curPos.getLatitude(), curPos.getLongitude());
  }

  @UiThread
  private void createNaviEngineTask(final Activity activity)
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
              MapHandler.getMapHandler().calcPath(recalcFrom.getLatitude(), recalcFrom.getLongitude(), recalcTo.getLatitude(), recalcTo.getLongitude(), activity);
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
          showInstruction(in);
        }
      }
    };
  }

  @UiThread
  private void showInstruction(NaviInstruction in)
  {
    if (in==null)
    {
      navtop_when.setText("0 " + UnitCalculator.getUnit(false));
      navtop_time.setText("--------");
      navtop_curloc.setText(R.string.search_location);
      navtop_nextloc.setText("==================");
      navtop_image.setImageResource(R.drawable.ic_2x_continue_on_street);
      setTiltMult(1);
    }
    else if(nearestP.isDirectionOk())
    {
      navtop_when.setText(in.getNextDistanceString());
      navtop_time.setText(in.getFullTimeString());
      navtop_curloc.setText(in.getCurStreet());
      navtop_nextloc.setText(in.getNextInstruction());
      navtop_image.setImageResource(in.getNextSignResource());
      setTiltMult(in.getNextDistance());
    }
    else
    {
      navtop_when.setText("0 " + UnitCalculator.getUnit(false));
      navtop_time.setText(in.getFullTimeString());
      navtop_curloc.setText(R.string.wrong_direction);
      navtop_nextloc.setText("==================");
      navtop_image.setImageResource(R.drawable.ic_2x_roundabout);
      setTiltMult(1);
    }
  }
  
  /** When next instruction has a distance of 400m then rotate tilt to see 400m far.
   *  Alternatively raise tilt on fast speed. **/
  private void setTiltMult(double nextDist)
  {
    double speedXtra = 0;
    if (pos!=null) { speedXtra = pos.getSpeed(); }
    if (speedXtra > 30.0) { speedXtra = 2; }
    else if (speedXtra < 8.0) { speedXtra = 0; }
    else
    {
      speedXtra = speedXtra - 8.0; // 0 - 22
      speedXtra = speedXtra / 22.0; // 0 - 1
      speedXtra = speedXtra * 2.0; // 0 - 2
    }
    if (nextDist > 400) { nextDist = 0; }
    else if (nextDist < 100) { nextDist = 0; }
    else
    {
      nextDist = nextDist - 100.0; // 0 - 300
      nextDist = nextDist / 300.0; // 0 - 1
      nextDist = nextDist * 2.0; // 0 - 2
    }
    if (speedXtra > nextDist) { nextDist = speedXtra; }
    tiltMultPos = (float)(1.0 + (nextDist * 0.5));
    tiltMult = (float)(1.0 + nextDist);
  }
  
  @WorkerThread
  private NaviInstruction calculatePosition(GeoPoint curPos)
  {
    if (uiJob == UiJob.RecalcPath) { return null; }
    if (uiJob == UiJob.Finished) { return null; }
    nearestP.setBaseData(getNearestPoint(instructions.get(0), nearestP.arrPos, curPos));
    
    if (nearestP.arrPos > 0)
    { // Check dist to line (backward)
      double lat1 = instructions.get(0).getPoints().getLatitude(nearestP.arrPos);
      double lon1 = instructions.get(0).getPoints().getLongitude(nearestP.arrPos);
      double lat2 = instructions.get(0).getPoints().getLatitude(nearestP.arrPos-1);
      double lon2 = instructions.get(0).getPoints().getLongitude(nearestP.arrPos-1);
      double lDist = GeoMath.distToLineSegment(curPos.getLatitude(), curPos.getLongitude(), lat1, lon1, lat2, lon2);
      if (lDist < nearestP.distance)
      {
        nearestP.distance = lDist;
        nearestP.status = PointPosData.Status.CurPosIsBackward;
      }
    }
    if (nearestP.arrPos < instructions.get(0).getPoints().size()-1)
    { // Check dist to line (forward)
      double lat1 = instructions.get(0).getPoints().getLatitude(nearestP.arrPos);
      double lon1 = instructions.get(0).getPoints().getLongitude(nearestP.arrPos);
      double lat2 = instructions.get(0).getPoints().getLatitude(nearestP.arrPos+1);
      double lon2 = instructions.get(0).getPoints().getLongitude(nearestP.arrPos+1);
      double lDist = GeoMath.distToLineSegment(curPos.getLatitude(), curPos.getLongitude(), lat1, lon1, lat2, lon2);
      if (lDist < nearestP.distance)
      {
        nearestP.distance = lDist;
        nearestP.status = PointPosData.Status.CurPosIsForward;
      }
    }
    else if (nearestP.arrPos == instructions.get(0).getPoints().size()-1 &&
             instructions.size()>1)
    {
      if (instructions.get(1).getPoints().size() > 0)
      { // Check dist to line (forward to next instruction)
        double lat1 = instructions.get(0).getPoints().getLatitude(nearestP.arrPos);
        double lon1 = instructions.get(0).getPoints().getLongitude(nearestP.arrPos);
        double lat2 = instructions.get(1).getPoints().getLatitude(0);
        double lon2 = instructions.get(1).getPoints().getLongitude(0);
        double lDist = GeoMath.distToLineSegment(curPos.getLatitude(), curPos.getLongitude(), lat1, lon1, lat2, lon2);
        if (lDist < nearestP.distance)
        {
          nearestP.distance = lDist;
          nearestP.status = PointPosData.Status.CurPosIsForward;
        }
      }
      if (instructions.get(1).getPoints().size() > 1)
      { // Check dist to line (forward next instruction p1+p2)
        double lat1 = instructions.get(1).getPoints().getLatitude(0);
        double lon1 = instructions.get(1).getPoints().getLongitude(0);
        double lat2 = instructions.get(1).getPoints().getLatitude(1);
        double lon2 = instructions.get(1).getPoints().getLongitude(1);
        double lDist = GeoMath.distToLineSegment(curPos.getLatitude(), curPos.getLongitude(), lat1, lon1, lat2, lon2);
        if (lDist < nearestP.distance)
        {
          nearestP.distance = lDist;
          nearestP.status = PointPosData.Status.CurPosIsForwardNext;
        }
      }
    }
    if (nearestP.isForward())
    {
      log("Reset bearing with calculatePosition()");
      nearestP.resetDirectionOk();
    }
    if (!nearestP.isDistanceOk())
    {
      double maxWayTolMeters = MAX_WAY_TOLERANCE_METER;
      if (directTargetDir) { maxWayTolMeters = maxWayTolMeters * 10.0; }
      Instruction nearestNext = instructions.find(curPos.getLatitude(), curPos.getLongitude(), maxWayTolMeters);
      if (nearestNext == null)
      {
        GeoPoint closestP = findClosestStreet(curPos);
        nearestNext = instructions.find(closestP.getLatitude(), closestP.getLongitude(), maxWayTolMeters);
      }
      if (nearestNext == null)
      {
        uiJob = UiJob.RecalcPath;
        recalcFrom = curPos;
        Instruction lastInstruction = instructions.get(instructions.size()-1);
        int lastPoint = lastInstruction.getPoints().size()-1;
        double lastPointLat = lastInstruction.getPoints().getLat(lastPoint);
        double lastPointLon = lastInstruction.getPoints().getLon(lastPoint);
        recalcTo = new GeoPoint(lastPointLat, lastPointLon);
log("NaviTask Start recalc !!!!!!");
        return null;
      }
      else
      { // Forward to nearest instruction.
        int deleteCounter = 0;
        Instruction lastDeleted = null;
        
        while (instructions.size()>0 && !instructions.get(0).equals(nearestNext))
        {
          deleteCounter++;
          lastDeleted = instructions.remove(0);
        }
        if (lastDeleted != null)
        { // Because we need the current, and not the next Instruction
          instructions.add(0, lastDeleted);
          deleteCounter --;
        }
        if (deleteCounter == 0)
        {
          PointPosData newNearestP = getNearestPoint(instructions.get(0), 0, curPos);
          //TODO: Continue-Instruction with DirectionInfo: getContinueInstruction() ?
log("NaviTask Start update far !!!!!!");
          return getUpdatedInstruction(curPos, newNearestP);
        }
log("NaviTask Start update skip-mult-" + deleteCounter + " !!!!!!");
        return getNewInstruction();
      }
    }
    else if (nearestP.isForwardNext())
    {
      instructions.remove(0);
log("NaviTask Start skip-next !!!!!!");
      return getNewInstruction();
    }
    else
    {
log("NaviTask Start update !!!!!!");
      return getUpdatedInstruction(curPos, nearestP);
    }
  }

  private static PointPosData getNearestPoint(Instruction instruction, int curPointPos, GeoPoint curPos)
  {
    int nextPointPos = curPointPos;
    int nearestPointPos = curPointPos;
    double nearestDist = Double.MAX_VALUE;
    while (instruction.getPoints().size() > nextPointPos)
    {
      double lat = instruction.getPoints().getLatitude(nextPointPos);
      double lon = instruction.getPoints().getLongitude(nextPointPos);
      double dist = GeoMath.fastDistance(curPos.getLatitude(), curPos.getLongitude(), lat, lon);
      if (dist < nearestDist)
      {
        nearestDist = dist;
        nearestPointPos = nextPointPos;
      }
      nextPointPos++;
    }
    PointPosData p = new PointPosData();
    p.arrPos = nearestPointPos;
    p.distance = nearestDist;
    return p;
  }

  private NaviInstruction getNewInstruction()
  {
    nearestP.arrPos = 0;
    nearestP.distance = Double.MAX_VALUE;
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
      if (speakDistanceCheck(in.getDistance()) && nearestP.isDirectionOk())
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
    uiJob = UiJob.Finished;
    return null;
  }
  
  private NaviInstruction getUpdatedInstruction(GeoPoint curPos, PointPosData nearestP)
  {
    uiJob = UiJob.UpdateInstruction;
    if (instructions.size() > 0)
    {
      Instruction in = instructions.get(0);
      long partTime = 0;
      double partDistance = countPartDistance(curPos, in, nearestP.arrPos);
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
      if (!naviVoiceSpoken && nearestP.isDirectionOk() && speakDistanceCheck(partDistance))
      {
        naviVoice.speak(newIn.getVoiceText());
        naviVoiceSpoken = true;
      }
      return newIn;
    }
    return null;
  }
  
  public void setNaviVoiceMute(boolean mute)
  {
    if (naviVoice!= null)
    {
      naviVoice.setTtsMute(mute);
    }
  }
  
  @UiThread
  private void resetNewInstruction()
  {
    nearestP.arrPos = 0;
    nearestP.distance = Double.MAX_VALUE;
    uiJob = UiJob.UpdateInstruction;
    showInstruction(null);
  }
  
  private boolean speakDistanceCheck(double dist)
  {
    if (dist < 200) { return true; }
    if (pos.getSpeed() > 150 * GeoMath.KMH_TO_MSEC)
    {
      if (dist < 1500) { return true; }
    }
    else if (pos.getSpeed() > 100 * GeoMath.KMH_TO_MSEC)
    {
      if (dist < 900) { return true; }
    }
    else if (pos.getSpeed() > 70 * GeoMath.KMH_TO_MSEC)
    {
      if (dist < 500) { return true; }
    }
    else if (pos.getSpeed() > 30 * GeoMath.KMH_TO_MSEC)
    {
      if (dist < 350) { return true; }
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

  private static void log(String str)
  {
    Log.i(NaviEngine.class.getName(), str);
  }
  
  static class PointPosData
  {
    public enum Status { CurPosIsExactly, CurPosIsBackward, CurPosIsForward, CurPosIsForwardNext };
    public int arrPos;
    public double distance;
    public Status status = Status.CurPosIsExactly;
    private boolean wrongDir = false;
    private boolean wrongDirHint = false;
    public boolean isDistanceOk()
    {
      return (distance < MAX_WAY_TOLERANCE);
    }
    public boolean isBackward() { return (status == Status.CurPosIsBackward); }
    public boolean isForward() { return (status == Status.CurPosIsForward); }
    public boolean isForwardNext() { return (status == Status.CurPosIsForwardNext); }
    public boolean isDirectionOk() { return (!wrongDir); }
    public void resetDirectionOk()
    {
      if (!isDistanceOk()) { return; }
      wrongDirHint = false;
      wrongDir = false;
    }
    public void checkDirectionOk(Location pos, Instruction in, NaviVoice v)
    {
      calculateWrongDir(pos, in);
      if (wrongDir)
      {
        if (wrongDirHint) { return; }
        wrongDirHint = true;
        v.speak("Wrong direction");
      }
    }
    
    private void calculateWrongDir(Location pos, Instruction in)
    {
      if (in.getPoints().size()<2) { return; }
      if (!wrongDir)
      {
        GeoPoint pathP1 = new GeoPoint(in.getPoints().getLat(0), in.getPoints().getLon(0));
        GeoPoint pathP2 = new GeoPoint(in.getPoints().getLat(1), in.getPoints().getLon(1));
        double bearingOk = pathP1.bearingTo(pathP2);
        double bearingCur = pos.getBearing();
        double bearingDiff = bearingOk - bearingCur;
        if (bearingDiff < 0) { bearingDiff += 360.0; } //Normalize
        if (bearingDiff > 180) { bearingDiff = 360.0 - bearingDiff; } //Normalize
        wrongDir = (bearingDiff > 100);
log("Compare bearing cur=" + bearingCur + " way=" + bearingOk + " wrong=" + wrongDir);
      }
    }
    
    public void setBaseData(PointPosData p)
    {
      this.arrPos = p.arrPos;
      this.distance = p.distance;
      this.status = p.status;
      if (arrPos > 0)
      {
log("Reset bearing with setBaseData()");
        resetDirectionOk();
      }
    }
  }
}
