package com.junjunguo.pocketmaps.geocoding;

import java.util.ArrayList;
import java.util.Locale;

import com.junjunguo.pocketmaps.util.GeoMath;
import com.junjunguo.pocketmaps.util.Variable;

import android.location.Address;

public class StreetMatcher extends CityMatcher
{
  public StreetMatcher(String searchS, boolean explicitSearch)
  {
    super(searchS, explicitSearch);
  }
  
  public static boolean addToList(ArrayList<Address> addressList, String name, double lat, double lon, Locale locale)
  {
    for (Address curAddr : addressList)
    {
      if (!curAddr.getAddressLine(GeocoderLocal.ADDR_TYPE_STREET).equals(name)) { continue; }
      double d = GeoMath.fastDistance(lat, lon, curAddr.getLatitude(), curAddr.getLongitude());
      d = d / GeoMath.DEGREE_PER_METER;
      if (d > 1000) { continue; }
      return false;
    }
    Address address = new Address(locale);
    address.setAddressLine(GeocoderLocal.ADDR_TYPE_COUNTRY, Variable.getVariable().getCountry());
    address.setLatitude(lat);
    address.setLongitude(lon);
    address.setAddressLine(GeocoderLocal.ADDR_TYPE_STREET, name);
    addressList.add(address);
    return true;
  }
}
