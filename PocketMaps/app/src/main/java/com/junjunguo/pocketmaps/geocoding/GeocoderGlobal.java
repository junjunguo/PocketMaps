package com.junjunguo.pocketmaps.geocoding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.osmdroid.location.GeocoderNominatim;

import com.junjunguo.pocketmaps.model.listeners.OnProgressListener;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public class GeocoderGlobal
{
  private static boolean stopping = false;
  Locale locale;
  
  public GeocoderGlobal(Locale locale)
  {
    this.locale = locale;
  }
  
  public static void stopRunningActions()
  {
    stopping = true;
  }
  
  protected static boolean isStopRunningActions()
  {
    return stopping;
  }
  
  public List<Address> find_google(Context context, String searchS)
  {
    stopping = false;
    log("Google geocoding started");
    Geocoder geocoder = new Geocoder(context, locale);
    if (!Geocoder.isPresent()) { return null; }
    try
    {
      List<Address> result = geocoder.getFromLocationName(searchS, 50);
      return result;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  public List<Address> find_osm(Context context, String searchS)
  {
    stopping = false;
    log("OSM geocoding started");
    Log.i(GeocoderGlobal.class.getName(), "OSM geocoding started");
    GeocoderNominatim geocoder = new GeocoderNominatim(context, locale);
    if (!GeocoderNominatim.isPresent()) { return null; }
    try
    {
      List<Address> result = geocoder.getFromLocationName(searchS, 50);
      return result;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  public List<Address> find_local(Context context, String searchS, OnProgressListener progressListener)
  {
    stopping = false;
    log("Local geocoding started");
    Log.i(GeocoderGlobal.class.getName(), "Offline geocoding started");
    GeocoderLocal geocoder = new GeocoderLocal(context, locale);
    if (!GeocoderLocal.isPresent()) { return null; }
    try
    {
      List<Address> result = geocoder.getFromLocationName(searchS, 50, progressListener);
      return result;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  private void log(String str)
  {
    Log.i(GeocoderGlobal.class.getName(), str);
  }
}
