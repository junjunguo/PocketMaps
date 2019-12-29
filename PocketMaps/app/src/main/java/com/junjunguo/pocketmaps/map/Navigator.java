package com.junjunguo.pocketmaps.map;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.listeners.NavigatorListener;
import com.junjunguo.pocketmaps.navigator.NaviEngine;
import com.junjunguo.pocketmaps.util.UnitCalculator;
import com.junjunguo.pocketmaps.util.Variable;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * singleton class
 * <p/>
 * Handler navigation information
 * <p/>
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 19, 2015.
 */
public class Navigator {
    /**
     * get from MapHandler calculate path
     */
    private PathWrapper ghResponse;
    /**
     * navigator is on or off
     */
    private boolean on;
    private List<NavigatorListener> listeners;
    private static Navigator navigator = null;


    private Navigator() {
        this.ghResponse = null;
        this.on = false;
        this.listeners = new ArrayList<>();
    }

    /**
     * @return Navigator object
     */
    public static Navigator getNavigator() {
        if (navigator == null) {
            navigator = new Navigator();
        }
        return navigator;
    }

    public PathWrapper getGhResponse() {
        return ghResponse;
    }

    public void setGhResponse(PathWrapper ghResponse) {
        this.ghResponse = ghResponse;
        if (NaviEngine.getNaviEngine().isNavigating())
        {
          NaviEngine.getNaviEngine().onUpdateInstructions(ghResponse.getInstructions());
        }
        else
        {
          setOn(ghResponse != null);
        }
    }

    /**
     * @param distance (<li>Instruction: return instructions distance </li>
     * @return a string  0.0 km (Exact one decimal place)
     */
    public String getDistance(Instruction distance) {
        if (distance.getSign() == Instruction.FINISH) return "";
        double d = distance.getDistance();
        return UnitCalculator.getString(d);
    }

    /**
     * @return distance of the whole journey
     */
    public String getDistance() {
        if (getGhResponse() == null) return UnitCalculator.getString(0);
        double d = getGhResponse().getDistance();
        return UnitCalculator.getString(d);
    }

    /**
     * @return a string time of the journey H:MM
     */
    public String getTime() {
        if (getGhResponse() == null) return " ";
        return getTimeString(getGhResponse().getTime());
    }
    
    public String getTimeString(long time)
    {
      int t = Math.round(time / 60000);
      if (t < 60) return t + " min";
      return t / 60 + " h: " + t % 60 + " m";
    }

    /**
     * @return a string time of the instruction min
     */
    public String getTime(Instruction time) {
        return Math.round(getGhResponse().getTime() / 60000) + " min";
    }


    /**
     * @return true is navigator is on
     */
    public boolean isOn() {
        return on;
    }

    /**
     * set navigator on or off
     *
     * @param on
     */
    public void setOn(boolean on) {
        this.on = on;
        broadcast();
    }
    
    public void setNaviStart(Activity activity, boolean on) {
        NaviEngine.getNaviEngine().setNavigating(activity, on);
        for (NavigatorListener listener : listeners) {
            listener.onNaviStart(on);
        }
    }

    /**
     * broadcast changes to listeners
     */
    protected void broadcast() {
        for (NavigatorListener listener : listeners) {
            listener.onStatusChanged(isOn());
        }
    }

    /**
     * add listener to listener list
     *
     * @param listener
     */
    public void addListener(NavigatorListener listener) {
        listeners.add(listener);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (ghResponse.getInstructions() != null) {
            for (Instruction i : ghResponse.getInstructions()) {
              sb.append("------>\ntime <long>: " + i.getTime());
              sb.append("\n");
              sb.append("name: street name" + i.getName());
              sb.append("\n");
              sb.append("annotation <InstructionAnnotation>");
              sb.append(i.getAnnotation().toString());
              sb.append("\n");
              sb.append("distance");
              sb.append(i.getDistance() + "\n");
              sb.append("sign <int>:" + i.getSign());
              sb.append("\n");
              sb.append("Points <PointsList>: " + i.getPoints());
              sb.append("\n");
            }
        }
        return sb.toString();
    }


    /**
     * this method can only used when Variable class is ready!
     *
     * @param dark (ask for dark icon resId ?)
     * @return int resId
     */
    public int getTravelModeResId(boolean dark) {
        if (dark)
        {
          if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Foot)
          {
            return R.drawable.ic_directions_walk_orange_24dp;
          }
          else if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Bike)
          {
            return R.drawable.ic_directions_bike_orange_24dp;
          }
          else if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Car)
          {
            return R.drawable.ic_directions_car_orange_24dp;
          }
        }
        else
        {
          if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Foot)
          {
            return R.drawable.ic_directions_walk_white_24dp;
          }
          else if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Bike)
          {
            return R.drawable.ic_directions_bike_white_24dp;
          }
          else if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Car)
          {
            return R.drawable.ic_directions_car_white_24dp;
          }
        }
        throw new NullPointerException("this method can only used when Variable class is ready!");
    }
    
    public int getTravelModeArrayIndex()
    {
      if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Foot)
      {
        return 0;
      }
      else if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Bike)
      {
        return 1;
      }
      else if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Car)
      {
        return 2;
      }
      throw new NullPointerException("this method can only used when Variable class is ready!");
    }
    
    public boolean setTravelModeArrayIndex(int index) {
      Variable.TravelMode selected;
      switch (index) {
          case 0:
              selected = Variable.TravelMode.Foot;
              break;
          case 1:
              selected = Variable.TravelMode.Bike;
              break;
          default:
              selected = Variable.TravelMode.Car;
      }
      Variable.getVariable().setTravelMode(selected);
      return Variable.getVariable().saveVariables(Variable.VarType.Base);
    }

    /**
     * @param itemData
     * @return int resId to instruction direction's sign icon
     */
    public int getDirectionSign(Instruction itemData) {
        switch (itemData.getSign()) {
            case Instruction.LEAVE_ROUNDABOUT:
                return R.drawable.ic_roundabout;
            case Instruction.TURN_SHARP_LEFT:
                return R.drawable.ic_turn_sharp_left;
            case Instruction.TURN_LEFT:
                return R.drawable.ic_turn_left;
            case Instruction.TURN_SLIGHT_LEFT:
                return R.drawable.ic_turn_slight_left;
            case Instruction.CONTINUE_ON_STREET:
                return R.drawable.ic_continue_on_street;
            case Instruction.TURN_SLIGHT_RIGHT:
                return R.drawable.ic_turn_slight_right;
            case Instruction.TURN_RIGHT:
                return R.drawable.ic_turn_right;
            case Instruction.TURN_SHARP_RIGHT:
                return R.drawable.ic_turn_sharp_right;
            case Instruction.FINISH:
                return R.drawable.ic_finish_flag;
            case Instruction.REACHED_VIA:
                return R.drawable.ic_reached_via;
            case Instruction.USE_ROUNDABOUT:
                return R.drawable.ic_roundabout;
            case Instruction.KEEP_RIGHT:
                return R.drawable.ic_keep_right;
            case Instruction.KEEP_LEFT:
                return R.drawable.ic_keep_left;
        }
        return 0;
    }
    
    /**
     * @param itemData
     * @return int resId to instruction direction's sign icon
     */
    public int getDirectionSignHuge(Instruction itemData) {
        switch (itemData.getSign()) {
            case Instruction.LEAVE_ROUNDABOUT:
                return R.drawable.ic_2x_roundabout;
            case Instruction.TURN_SHARP_LEFT:
                return R.drawable.ic_2x_turn_sharp_left;
            case Instruction.TURN_LEFT:
                return R.drawable.ic_2x_turn_left;
            case Instruction.TURN_SLIGHT_LEFT:
                return R.drawable.ic_2x_turn_slight_left;
            case Instruction.CONTINUE_ON_STREET:
                return R.drawable.ic_2x_continue_on_street;
            case Instruction.TURN_SLIGHT_RIGHT:
                return R.drawable.ic_2x_turn_slight_right;
            case Instruction.TURN_RIGHT:
                return R.drawable.ic_2x_turn_right;
            case Instruction.TURN_SHARP_RIGHT:
                return R.drawable.ic_2x_turn_sharp_right;
            case Instruction.FINISH:
                return R.drawable.ic_2x_finish_flag;
            case Instruction.REACHED_VIA:
                return R.drawable.ic_2x_reached_via;
            case Instruction.USE_ROUNDABOUT:
                return R.drawable.ic_2x_roundabout;
            case Instruction.KEEP_RIGHT:
                return R.drawable.ic_2x_keep_right;
            case Instruction.KEEP_LEFT:
              return R.drawable.ic_2x_keep_left;
        }
        return 0;
    }

    /**
     * @param instruction
     * @return direction
     */
    public String getDirectionDescription(Instruction instruction, boolean longText) {
        if (instruction.getSign() == 4) return "Navigation End";//4
        String str; // TODO: Translate all this instructions to Language?
        String streetName = instruction.getName();
        int sign = instruction.getSign();
        String dir = "";
        String dirTo = "onto";
        switch (sign) {
            case Instruction.CONTINUE_ON_STREET:
                dir = ("Continue");
                dirTo = "on";
                break;
            case Instruction.LEAVE_ROUNDABOUT:
                dir = ("Leave roundabout");
                break;
            case Instruction.TURN_SHARP_LEFT:
                dir = ("Turn sharp left");
                break;
            case Instruction.TURN_LEFT:
                dir = ("Turn left");
                break;
            case Instruction.TURN_SLIGHT_LEFT:
                dir = ("Turn slight left");
                break;
            case Instruction.TURN_SLIGHT_RIGHT:
                dir = ("Turn slight right");
                break;
            case Instruction.TURN_RIGHT:
                dir = ("Turn right");
                break;
            case Instruction.TURN_SHARP_RIGHT:
                dir = ("Turn sharp right");
                break;
            case Instruction.REACHED_VIA:
                dir = ("Reached via");
                break;
            case Instruction.USE_ROUNDABOUT:
                dir = ("Use roundabout");
                break;
            case Instruction.KEEP_LEFT:
              dir = ("Keep left");
              break;
            case Instruction.KEEP_RIGHT:
              dir = ("Keep right");
              break;
        }
        if (!longText) { return dir; }
        str = Helper.isEmpty(streetName) ? dir : (dir + " " + dirTo + " " + streetName);
        return str;
    }

}
