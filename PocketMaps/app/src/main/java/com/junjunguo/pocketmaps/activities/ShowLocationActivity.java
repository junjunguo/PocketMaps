package com.junjunguo.pocketmaps.activities;

import java.util.Properties;
import java.util.Set;

import org.oscim.core.GeoPoint;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/** This Activity shows a location via MainActivity, selected from an other app. **/
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
      if (getLocationFromQueryParameters(prop, uri)) {}
      else if (getLocationFromParsingParameters(prop, uri)) {}
      else if (getLocationFromParsingValues(prop, uri)) {}
      else
      {
        throw new IllegalArgumentException("Input-uri has no parameters!");
      }
      locationSearchString = prop.getProperty("q");
      if (locationSearchString!=null)
      {
        locationSearchString = java.net.URLDecoder.decode(locationSearchString, java.nio.charset.StandardCharsets.UTF_8.toString());
        return;
      }
      String lat = prop.getProperty("lat");
      String lon = prop.getProperty("lon");
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
  
  private boolean getLocationFromParsingValues(Properties prop, Uri uri)
  {
    log("Try parsing values directly.");
    String schemePart = uri.getEncodedSchemeSpecificPart();
    if (schemePart == null) { return false; }
    int indexQ = schemePart.indexOf("?");
    if (indexQ>=0) { schemePart = schemePart.substring(0,indexQ); }
    String latLon[] = schemePart.split(",");
    if (latLon.length != 2 && latLon.length != 3) { return false; } // lat,lon[,alt]
    prop.put("lat", latLon[0]);
    prop.put("lon", latLon[1]);
    return checkLocationParameters(prop, "ParsingValues");
  }

  private boolean getLocationFromParsingParameters(Properties prop, Uri uri)
  {
    log("Try parsing parameters directly.");
    int index = uri.toString().indexOf("?");
    if (index < 0) { return false; }
    String[] values = uri.toString().substring(index+1).split("\\&");
    for (String v : values)
    {
      index = v.indexOf('=');
      if (index < 0) { continue; }
      String key = v.substring(0, index);
      String val = v.substring(index+1);
      prop.setProperty(key, val);
    }
    return checkLocationParameters(prop, "ParsingParameters");
  }

  private boolean getLocationFromQueryParameters(Properties prop, Uri uri)
  {
    log("Try parsing query-parameters.");
    try
    {
      Set<String> keys = uri.getQueryParameterNames();
      for (String key : keys)
      {
        String value = uri.getQueryParameter(key);
        prop.setProperty(key, value);
      }
      return checkLocationParameters(prop, "QueryParameters");
    }
    catch (UnsupportedOperationException e)
    {
      log("Uri has no query-parameters");
      // Example: "geo:0,0?q=MySearchLocation"
      // Example: "geo:100,100"
      return false;
    }
  }

  private boolean checkLocationParameters(Properties prop, String info)
  {
    if (prop.size() == 0) { log("No param result: " + info); return false; }
    if (prop.get("q") != null) { return true; }
    if (prop.get("lat") == null) { log("No lat-param result: " + info); return false; }
    if (prop.get("lon") == null) { log("No lon-param result: " + info); return false; }
    return true;
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

