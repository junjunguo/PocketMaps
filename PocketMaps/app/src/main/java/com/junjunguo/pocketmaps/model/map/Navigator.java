package com.junjunguo.pocketmaps.model.map;

import com.graphhopper.GHResponse;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.junjunguo.pocketmaps.model.util.NavigatorListener;

import org.mapsforge.map.android.view.MapView;

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
    private MapView mapView;
    private GHResponse ghResponse;
    private InstructionList instructionsList;
    private boolean on;
    private List<NavigatorListener> listeners;
    private String vehicle, weighting;
    private static Navigator navigator = null;


    private Navigator() {
        this.mapView = null;
        this.ghResponse = null;
        this.on = false;
        this.listeners = new ArrayList<>();
        this.vehicle = "foot";
        this.weighting = "fastest";
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

            setInstructionsList(null);
        } else {
            setInstructionsList(ghResponse.getInstructions());
        }
        setOn(ghResponse != null);
    }

    public MapView getMapView() {
        return mapView;
    }

    public void setMapView(MapView mapView) {
        this.mapView = mapView;
    }

    public InstructionList getInstructionsList() {
        return instructionsList;
    }

    private void setInstructionsList(InstructionList instructionsList) {
        this.instructionsList = instructionsList;
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
    protected void setOn(boolean on) {
        this.on = on;
        broadcast();
    }


    /**
     * @return car, foot or bike
     */
    public String getVehicle() {
        return vehicle;
    }

    /**
     * @param vehicle: car, foot or bike
     */
    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getWeighting() {
        return weighting;
    }

    public void setWeighting(String weighting) {
        this.weighting = weighting;
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
        if (getInstructionsList() != null) {
            for (Instruction i : getInstructionsList()) {
                s += i.toString() + "\n";
            }
        }
        return s;
    }
}
