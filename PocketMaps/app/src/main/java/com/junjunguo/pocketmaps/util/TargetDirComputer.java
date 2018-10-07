package com.junjunguo.pocketmaps.util;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionAnnotation;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.graphhopper.util.Translation;
import com.junjunguo.pocketmaps.map.Tracking;

import android.util.Log;

public class TargetDirComputer
{
  private static TargetDirComputer instance = new TargetDirComputer();
  
  public static TargetDirComputer getInstance()
  {
    return instance;
  }

  public GHResponse createTargetdirResponse(double fromLat, double fromLon, double toLat, double toLon)
  {
    GHResponse resp = new GHResponse();
    PathWrapper pr = new PathWrapper();
    PointList pl = new PointList();
    pl.add(fromLat, fromLon);
    pl.add(toLat, toLon);
    pr.setPoints(pl);
    double distance = GeoMath.fastDistance(fromLat, fromLon, toLat, toLon);
    distance = distance * GeoMath.METER_PER_DEGREE;
    pr.setDistance(distance);
    double distKm = distance / 1000.0;
    double hours = distKm / 50; // 50km == 1h
    if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Bike) { hours = hours * 3.0; }
    if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Foot) { hours = hours * 9.0; }
    long timeMs = (long)(hours * 60.0 * 60.0 * 1000.0);
    pr.setTime(timeMs);
    InstructionList insL = new InstructionList(createEmptyTranslator());
    int sign = Instruction.CONTINUE_ON_STREET;
    String name = "direction to target";
    InstructionAnnotation ia = InstructionAnnotation.EMPTY;
    Instruction ins = new Instruction(sign, name, ia, pl);
    ins.setDistance(distance);
    ins.setTime(timeMs);
    insL.add(ins);
    pr.setInstructions(insL);
    resp.add(pr);
    return resp;
  }

  private Translation createEmptyTranslator()
  {
    Translation t = new Translation()
    {
      @Override public String tr(String key, Object... params)
      {
        StringBuilder sb = new StringBuilder(key);
        for (Object o : params) { sb.append(" ").append(o.toString()); }
        return sb.toString();
      }

      @Override public Map<String, String> asMap()
      {
        return new HashMap<String, String>();
      }

      @Override public Locale getLocale() { return Locale.ENGLISH; }

      @Override public String getLanguage() { return Locale.ENGLISH.getLanguage(); }
    };
    return t;
  }
}
