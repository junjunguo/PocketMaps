package com.junjunguo.pocketmaps.model.map;

import com.graphhopper.GHResponse;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.listeners.NavigatorListener;
import com.junjunguo.pocketmaps.model.util.Variable;

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
    private GHResponse ghResponse;
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

    public GHResponse getGhResponse() {
        return ghResponse;
    }

    public void setGhResponse(GHResponse ghResponse) {
        this.ghResponse = ghResponse;
        if (ghResponse == null) {

        } else {
        }
        setOn(ghResponse != null);
    }

    /**
     * @param distance (<li>Instruction: return instructions distance </li>
     * @return a string  0.0 km (Exact one decimal place)
     */
    public String getDistance(Instruction distance) {
        if (distance.getSign() == 4) return "";//finished
        double d = distance.getDistance();
        if (d < 1000) return Math.round(d) + " meter";
        return (((int) (d / 100)) / 10f) + " km";
    }

    /**
     * @return distance of the whole journey
     */
    public String getDistance() {
        if (getGhResponse() == null) return 0 + " " + R.string.km;
        double d = getGhResponse().getDistance();
        if (d < 1000) return Math.round(d) + " meter";
        return (((int) (d / 100)) / 10f) + " km";
    }

    /**
     * @return a string time of the journey H:MM
     */
    public String getTime() {
        if (getGhResponse() == null) return " ";
        int t = Math.round(getGhResponse().getMillis() / 60000);
        if (t < 60) return t + " min";
        return t / 60 + " h: " + t % 60 + " m";
    }

    /**
     * @return a string time of the instruction min
     */
    public String getTime(Instruction time) {
        return Math.round(getGhResponse().getMillis() / 60000) + " min";
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

    /**
     * broadcast changes to listeners
     */
    protected void broadcast() {
        for (NavigatorListener listener : listeners) {
            listener.statusChanged(isOn());
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
        String s = "";
        if (ghResponse.getInstructions() != null) {
            for (Instruction i : ghResponse.getInstructions()) {
                s += "------>\ntime <long>: " + i.getTime() + "\n" + "name: street name" + i.getName() + "\n" +
                        "annotation <InstructionAnnotation>" +
                        i.getAnnotation() + "\n" + "distance" + i.getDistance() + "\n" + "sign <int>:" + i.getSign() +
                        "\n" + "Points <PointsList>: " + i.getPoints() + "\n";
            }
        }
        return s;
    }


    /**
     * this method can only used when Variable class is ready!
     *
     * @param dark (ask for dark icon resId ?)
     * @return int resId
     */
    public int getTravelModeResId(boolean dark) {
        if (dark) {
            switch (Variable.getVariable().getTravelMode()) {
                case "foot":
                    return R.drawable.ic_directions_walk_orange_24dp;
                case "bike":
                    return R.drawable.ic_directions_bike_orange_24dp;
                case "car":
                    return R.drawable.ic_directions_car_orange_24dp;
            }
        } else {
            switch (Variable.getVariable().getTravelMode()) {
                case "foot":
                    return R.drawable.ic_directions_walk_white_24dp;
                case "bike":
                    return R.drawable.ic_directions_bike_white_24dp;
                case "car":
                    return R.drawable.ic_directions_car_white_24dp;
            }
        }
        throw new NullPointerException("this method can only used when Variable class is ready!");
    }

    /**
     * @param itemData
     * @return int resId to instruction direction's sign icon
     */
    public int getDirectionSign(Instruction itemData) {
        switch (itemData.getSign()) {
            case -6:
                return R.drawable.ic_roundabout;
            case -3:
                return R.drawable.ic_turn_sharp_left;
            case -2:
                return R.drawable.ic_turn_left;
            case -1:
                return R.drawable.ic_turn_slight_left;
            case 0:
                return R.drawable.ic_continue_on_street;
            case 1:
                return R.drawable.ic_turn_slight_right;
            case 2:
                return R.drawable.ic_turn_right;
            case 3:
                return R.drawable.ic_turn_sharp_right;
            case 4:
                return R.drawable.ic_finish_flag;
            case 5:
                return R.drawable.ic_reached_via;
            case 6:
                return R.drawable.ic_roundabout;
        }
        return 0;
    }

    /**
     * LEAVE_ROUNDABOUT = -6; -
     * <p/>
     * TURN_SHARP_LEFT = -3;
     * <p/>
     * TURN_LEFT = -2;
     * <p/>
     * TURN_SLIGHT_LEFT = -1;
     * <p/>
     * CONTINUE_ON_STREET = 0;
     * <p/>
     * TURN_SLIGHT_RIGHT = 1;
     * <p/>
     * TURN_RIGHT = 2;
     * <p/>
     * TURN_SHARP_RIGHT = 3;
     * <p/>
     * FINISH = 4;
     * <p/>
     * REACHED_VIA = 5; -
     * <p/>
     * USE_ROUNDABOUT = 6 -;
     *
     * @param instruction
     * @return direction
     */
    public String getDirectionDescription(Instruction instruction) {
        if (instruction.getSign() == 4) return "Navigation End";//4
        String str;
        String streetName = instruction.getName();
        int sign = instruction.getSign();
        if (sign == Instruction.CONTINUE_ON_STREET) {//0
            str = Helper.isEmpty(streetName) ? "Continue" : ("Continue onto " + streetName);
        } else {
            String dir = "";
            switch (sign) {
                case Instruction.LEAVE_ROUNDABOUT://-6
                    dir = ("Leave roundabout");
                    break;
                case Instruction.TURN_SHARP_LEFT://-3
                    dir = ("Turn sharp left");
                    break;
                case Instruction.TURN_LEFT://-2
                    dir = ("Turn left");
                    break;
                case Instruction.TURN_SLIGHT_LEFT://-1
                    dir = ("Turn slight left");
                    break;
                case Instruction.TURN_SLIGHT_RIGHT://1
                    dir = ("Turn slight right");
                    break;
                case Instruction.TURN_RIGHT://2
                    dir = ("Turn right");
                    break;
                case Instruction.TURN_SHARP_RIGHT://3
                    dir = ("Turn sharp right");
                    break;
                case Instruction.REACHED_VIA://5
                    dir = ("Reached via");
                    break;
                case Instruction.USE_ROUNDABOUT://6
                    dir = ("Use roundabout");
                    break;
            }
            str = Helper.isEmpty(streetName) ? dir : (dir + " onto " + streetName);
        }
        return str;
    }

}
