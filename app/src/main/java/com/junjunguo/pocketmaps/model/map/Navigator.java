package com.junjunguo.pocketmaps.model.map;

import com.graphhopper.GHResponse;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import org.mapsforge.map.android.view.MapView;

/**
 * singleton class
 * <p>
 * Handler navigation information
 * <p>
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on June 19, 2015.
 */
public class Navigator {
    private MapView mapView;
    private GHResponse ghResponse;
    private InstructionList instructionsList;
    private Instruction instruction;

    private static Navigator navigator = null;

    private Navigator() {
        this.mapView = null;
        this.ghResponse = null;
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
        setInstructionsList(ghResponse.getInstructions());
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

    public Instruction getInstruction() {
        return instruction;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
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
