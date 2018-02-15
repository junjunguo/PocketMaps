package com.junjunguo.pocketmaps.geocoding;

import java.util.Locale;
import java.util.Properties;
import java.util.Map.Entry;

import org.oscim.core.GeoPoint;

import com.junjunguo.pocketmaps.util.Variable;

import android.location.Address;
import android.util.Log;

public class AddressLoc
{
  private static final char STRING_SEP = '|';
  private static final char STRING_SEP_ESC = '/';
  public boolean isAdded = false; //Added to list (flag)
  public String city;
  public String street;
  public String postalCode;
  public GeoPoint location;
  
  public AddressLoc ensureCopy()
  {
    if (isAdded) { return new AddressLoc(); }
    city = null;
    street = null;
    postalCode = null;
    location = null;
    return this;
  }
  
  public Address toAndroidAddress(Locale locale)
  {
    Address address = new Address(locale);
    address.setPostalCode(postalCode);
    address.setThoroughfare(street);
    address.setSubAdminArea(city);
    address.setAddressLine(0, street);
    address.setAddressLine(1, postalCode);
    address.setAddressLine(2, city);
    address.setAddressLine(3, Variable.getVariable().getCountry());
    address.setLatitude(location.getLatitude());
    address.setLongitude(location.getLongitude());
    return address;
  }
  
  public static boolean addToProp(Properties prop, Address addr)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(addr.getAddressLine(1).replace(STRING_SEP, STRING_SEP_ESC));
    sb.append('|');
    sb.append(addr.getAddressLine(2).replace(STRING_SEP, STRING_SEP_ESC));
    sb.append('|');
    sb.append(addr.getAddressLine(3).replace(STRING_SEP, STRING_SEP_ESC));
    sb.append('|');
    sb.append(addr.getLatitude());
    sb.append('|');
    sb.append(addr.getLongitude());
    prop.put(addr.getAddressLine(0), sb.toString());
    return true;
  }
  
  public static Address readFromPropEntry(Entry<Object, Object> entry)
  {
    Address addr = new Address(Locale.getDefault());
    addr.setAddressLine(0, entry.getKey().toString());
    String[] lines = entry.getValue().toString().split("\\" + STRING_SEP);
    if (lines.length!=5)
    {
      log("Can not read Favourite because of line.length = " + lines.length);
      return null;
    }
    addr.setAddressLine(1, lines[0]);
    addr.setAddressLine(2, lines[1]);
    addr.setAddressLine(3, lines[2]);
    try
    {
      addr.setLatitude(Double.parseDouble(lines[3]));
      addr.setLongitude(Double.parseDouble(lines[4]));
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
