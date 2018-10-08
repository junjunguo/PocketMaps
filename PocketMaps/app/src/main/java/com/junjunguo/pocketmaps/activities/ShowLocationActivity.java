package com.junjunguo.pocketmaps.activities;

import java.util.Properties;
import java.util.Set;

import org.oscim.core.GeoPoint;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class ShowLocationActivity  extends AppCompatActivity
{
  public static String locationSearchString;
  public static GeoPoint locationGeoPoint;
  
  @Override protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    handleInput();
    showMain();
  }

  private void handleInput()
  {
    final Intent intent = getIntent();
    try
    {
      Uri uri = intent.getData();
      log("--> Got input uri: " + uri);
      Properties prop = new Properties();
      try
      {
        Set<String> keys = uri.getQueryParameterNames();
        for (String key : keys)
        {
          String value = uri.getQueryParameter(key);
          prop.setProperty(key, value);
          log("--> Got input: " + key + "=" + value);
        }
      }
      catch (UnsupportedOperationException e)
      { // Example: "geo:0,0?q=MySearchLocation"
        log("Uri has no parameters, try parsing.");
        int index = uri.toString().indexOf("?");
        if (index < 0) { throw new IllegalArgumentException("Input-uri has no parameters!"); }
        String[] values = uri.toString().substring(index+1).split("\\&");
        for (String v : values)
        {
          index = v.indexOf('=');
          if (index < 0) { continue; }
          String key = v.substring(0, index);
          String val = v.substring(index+1);
          prop.setProperty(key, val);
        }
      }
      locationSearchString = prop.getProperty("q");
      if (locationSearchString!=null) { return; }
      String lat = prop.getProperty("lat");
      String lon = prop.getProperty("lat");
      if (lat==null || lon==null)
      {
        throw new IllegalArgumentException("Missing input!");
      }
      double latD = Double.parseDouble(lat);
      double lonD = Double.parseDouble(lon);
      locationGeoPoint = new GeoPoint(latD, lonD);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      logUser("Error getting input: " + e.getMessage());
    }
  }

  @Override protected void onResume()
  {
    super.onResume();
  }
  
  @Override protected void onDestroy()
  {
    super.onDestroy();
  }
  
  private void showMain()
  {
    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra("com.junjunguo.pocketmaps.activities.MapActivity.SELECTNEWMAP", false);
    startActivity(intent);
    finish();
  }
  
  private void log(String str)
  {
    Log.i(ShowLocationActivity.class.getName(), str);
  }
    
  private void logUser(String str)
  {
    Log.i(ShowLocationActivity.class.getName(), str);
    try
    {
      Toast.makeText(this.getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }
    catch (Exception e) { e.printStackTrace(); }
  }
}

