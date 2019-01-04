package com.junjunguo.pocketmaps.geocoding;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.Map.Entry;

import com.junjunguo.pocketmaps.util.Variable;

import android.location.Address;
import android.util.Log;

public class AddressLoc
{
  private static final char STRING_SEP = '|';
  private static final char STRING_SEP_ESC = '/';
  
  /** Adds the address to prop.
   *  @param updateLocName The old name of address (=firstLine) to update **/
  public static void addToProp(Properties prop, Address address, String updateLocName)
  {
    if (updateLocName!=null) { prop.remove(updateLocName); }
    ArrayList<String> addr = getLines(address);
    StringBuilder sb = new StringBuilder();
    for (int i=1; i<addr.size(); i++)
    {
      sb.append(addr.get(i).replace(STRING_SEP, STRING_SEP_ESC));
      sb.append('|');
    }
    sb.append(address.getLatitude());
    sb.append('|');
    sb.append(address.getLongitude());
    String addrName = addr.get(0);
    if (prop.containsKey(addrName))
    {
      if (updateLocName!=null)
      { // Use old name instead of overriding!
        addrName = updateLocName;
      }
      else
      {
        int index = 0;
        while(prop.containsKey(addrName + "_" + index)) { index++; }
        addrName = addrName + "_" + index;
      }
    }
    prop.put(addrName, sb.toString());
  }
  
  /** Gets all lines from address.
   *  @return Array with a length of min 1. **/
  public static ArrayList<String> getLines(Address address)
  {
    ArrayList<String> list = new ArrayList<String>();
    putLine(list, address.getFeatureName());
    putLine(list, address.getThoroughfare());
    putLine(list, address.getUrl());
    putLine(list, address.getPostalCode());
    putLine(list, address.getSubThoroughfare());
    putLine(list, address.getPremises());
    putLine(list, address.getSubAdminArea());
    putLine(list, address.getAdminArea());
    putLine(list, address.getCountryCode());
    putLine(list, address.getCountryName());
    putLine(list, address.getSubLocality());
    putLine(list, address.getLocality());
    putLine(list, address.getPhone());
    for (int i=0; i<=address.getMaxAddressLineIndex(); i++)
    {
      putLine(list, address.getAddressLine(i));
    }
    if (list.size() == 0) { list.add(Variable.getVariable().getCountry()); }
    return list;
  }

  private static void putLine(ArrayList<String> list, String line)
  {
    if (line == null) { return; }
    if (line.isEmpty()) { return; }
    if (list.contains(line)) { return; }
    list.add(line);
  }

  public static Address readFromPropEntry(Entry<Object, Object> entry)
  {
    Address addr = new Address(Locale.getDefault());
    addr.setAddressLine(0, entry.getKey().toString());
    String[] lines = entry.getValue().toString().split("\\" + STRING_SEP, -1);
    for (int i=0; i<(lines.length-2); i++)
    {
      addr.setAddressLine(i+1, lines[i]);
    }
    try
    { // Last two values are lat and lon!
      addr.setLatitude(Double.parseDouble(lines[lines.length-2]));
      addr.setLongitude(Double.parseDouble(lines[lines.length-1]));
    }
    catch (NumberFormatException e)
    {
      log("Can not read lat lon from stored Favourite!");
      return null;
    }
    return addr;
  }
  
  private static void log(String str)
  {
    Log.i(AddressLoc.class.getName(), str);
  }
}
