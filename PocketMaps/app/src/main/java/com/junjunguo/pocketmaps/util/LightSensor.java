package com.junjunguo.pocketmaps.util;

import com.junjunguo.pocketmaps.navigator.NaviEngine;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class LightSensor implements SensorEventListener
{
  public enum Brightness{DARK, MID, LIGHT, USER};
  boolean isSupported = false;
  Window affectedWin;
  float default_value = Float.MAX_VALUE;
  
  public LightSensor(Activity activity)
  {
    SensorManager mySensorManager = (SensorManager)activity.getSystemService(android.app.Service.SENSOR_SERVICE);
    Sensor lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    if(lightSensor != null)
    {
      mySensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL, 3000);
      isSupported = true;
    }
    affectedWin = activity.getWindow();
  }
  
  public boolean isSupported() { return isSupported; }
  
  public void cleanup(Activity activity)
  {
    setLight(Brightness.USER);
    SensorManager mySensorManager = (SensorManager)activity.getSystemService(android.app.Service.SENSOR_SERVICE);
    Sensor lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    if(lightSensor != null)
    {
      mySensorManager.unregisterListener(this, lightSensor);
    }
    affectedWin = null;
  }
  
  private void setLight(Brightness b)
  {
    WindowManager.LayoutParams lp = affectedWin.getAttributes();
    if (b != Brightness.USER)
    {
      if (!Variable.getVariable().isLightSensorON()) { return; }
      if (default_value == Float.MAX_VALUE)
      {
        default_value = lp.screenBrightness;
      }
      float newValue = -1.0f;
      if (b == Brightness.DARK && lp.screenBrightness != 0.0f) { newValue = 0.0f; }
      else if (b == Brightness.MID && lp.screenBrightness != 0.5f) { newValue = 0.5f; }
      else if (b == Brightness.LIGHT && lp.screenBrightness != 1.0f) { newValue = 1.0f; }
      if (newValue != -1)
      {
        log("LightChange, set " + b + " display!");
        lp.screenBrightness = newValue;
        affectedWin.setAttributes(lp);
      }
    }
    else
    {
      if (default_value != Float.MAX_VALUE)
      {
        log("LightChange, set default display!");
        lp.screenBrightness = default_value;
        default_value = Float.MAX_VALUE;
        affectedWin.setAttributes(lp);
      }
    }
  }
  
  @Override
  public void onSensorChanged(SensorEvent event)
  {
    if(event.sensor.getType() == Sensor.TYPE_LIGHT)
    {
      if (NaviEngine.getNaviEngine().isNavigating())
      {
        if (event.values[0] <= 0.0f)
        {
          setLight(Brightness.DARK);
        }
        else if (event.values[0] <= 1000.0f)
        {
          setLight(Brightness.MID);
        }
        else if (event.values[0] > 1000.0f)
        {
          setLight(Brightness.LIGHT);
        }
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy)
  {
  }
  
  private static void log(String str)
  {
    Log.i(LightSensor.class.getName(), str);
  }
}
