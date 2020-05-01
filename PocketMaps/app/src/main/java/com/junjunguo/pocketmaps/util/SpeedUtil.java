package com.junjunguo.pocketmaps.util;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.UiThread;
import com.graphhopper.util.details.PathDetail;
import com.junjunguo.pocketmaps.map.Navigator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Starcommander@github.com
 */
public class SpeedUtil
{
//    ArrayList<SpeedArea> maxSpeedList = new ArrayList<SpeedArea>();
//    ArrayList<SpeedArea> aveSpeedList = new ArrayList<SpeedArea>();
    List<PathDetail> maxSpeedList;
    List<PathDetail> aveSpeedList;
    
    TextView view;
    boolean enabled = false;
    int pointsDone = 0;
    
    public SpeedUtil() {}
    
    public void initTextView(TextView view)
    {
        this.view = view;
        updateViewVis();
    }
    
    @UiThread
    public void updateTextView(int pointNum)
    {
        if (!enabled) { return; }
        if (view==null) { Log.w(SpeedUtil.class.getName(), "Missing view for speed!"); }
        else
        {
            view.setText(getSpeedValue(pointNum + pointsDone));
        }
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        updateViewVis();
        if (view != null)
        { view.setVisibility(View.GONE); }
    }
    
    private void updateViewVis()
    {
        if (view == null) { return; }
        if (enabled) { view.setVisibility(View.VISIBLE); }
        else { view.setVisibility(View.GONE); }
    }
    
    public void updateList(Map<String, List<PathDetail>> pathDetails)
    {
        maxSpeedList = pathDetails.get("max_speed");
        aveSpeedList = pathDetails.get("average_speed");
        pointsDone = 0;
//        for (java.util.Map.Entry e : pathDetails.entrySet())
//        { //TODO: Delete this debug info
//            System.out.println("PD: " + e.getKey() + "=" + e.getValue());
//        }
    }
    
    public void updateInstructionDone(int pLen)
    {
        pointsDone += pLen;
    }
    
    private String getSpeedValue(int pos)
    {
        if (maxSpeedList == null) { return "---"; }
        if (aveSpeedList == null) { return "---"; }
        for (PathDetail curDetail : maxSpeedList)
        {
            if (curDetail.getFirst() > pos) { continue; }
            if (curDetail.getLast() < pos) { continue; }
            if (curDetail.getValue()==null) { continue; }
//System.out.println("PD: " + pos + " MaxSpeed=" + curDetail.getValue());
            return "" + ((int)Double.parseDouble(curDetail.getValue().toString()));
        }
        for (PathDetail curDetail : aveSpeedList)
        {
            if (curDetail.getFirst() > pos) { continue; }
            if (curDetail.getLast() < pos) { continue; }
            if (curDetail.getValue()==null) { continue; }
//System.out.println("PD: " + pos + " AvgSpeed=" + curDetail.getValue());
            return "" + ((int)Double.parseDouble(curDetail.getValue().toString())) + "?";
        }
        return "---";
    }

}
