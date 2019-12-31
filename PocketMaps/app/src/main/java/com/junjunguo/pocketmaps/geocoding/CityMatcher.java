package com.junjunguo.pocketmaps.geocoding;

public class CityMatcher
{
  String lines[];
  boolean isNumeric[];

  public CityMatcher(String searchS, boolean explicitSearch)
  {
    if (explicitSearch)
    {
      lines = new String[1];
      lines[0] = searchS;
    }
    else
    {
      lines = searchS.replace('\n', ' ').split(" ");
    }
    isNumeric = new boolean[lines.length];
    for (int i=0; i<lines.length; i++)
    {
      isNumeric[i] = isNumeric(lines[i]);
      if (!isNumeric[i])
      {
        lines[i] = lines[i].toLowerCase();
      }
    }
  }
  
  public boolean isMatching(String value, boolean valueNumeric)
  {
    if (value.isEmpty()) { return false; }
    if (!valueNumeric) { value = value.toLowerCase(); }
    for (int i=0; i<lines.length; i++)
    {
      if (lines[i].isEmpty()) { continue; }
      if (valueNumeric && isNumeric[i])
      {
        if (value.equals(lines[i])) { return true; }
      }
      if (!valueNumeric && !isNumeric[i])
      {
        if (lines[i].length() < 3)
        {
          if (value.equals(lines[i])) { return true; }
        }
        if (value.contains(lines[i])) { return true; }
      }
    }
    return false;
  }
  
  /** Ignores ',' and '.' on check. **/
  public static boolean isNumeric(String s)
  {
    try
    {
      Integer.parseInt(s.replace('.', '0').replace(',', '0'));
      return true;
    }
    catch (NumberFormatException e)
    {
      return false;
    }
  }
}
