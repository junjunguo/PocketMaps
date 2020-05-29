package com.junjunguo.pocketmaps.util;

import org.json.JSONException;
import org.json.JSONObject;

/** This class is a wrapper of JSONObject, that ensures, no exception will be thrown on reading missing value. Uses defValue instead. **/

public class JsonWrapper
{
    JSONObject obj;
    
    public JsonWrapper()
    {
        obj = new JSONObject();
    }
    
    public JsonWrapper(String content) throws JSONException
    {
        obj = new JSONObject(content);
    }
    
    public void put(String key, boolean v) throws JSONException { obj.put(key, v); }
    public void put(String key, int v) throws JSONException { obj.put(key, v); }
    public void put(String key, Object v) throws JSONException { obj.put(key, v); }
    
    // Check first to ensure, no exception will be thrown.
    public boolean getBool(String key, boolean def) throws JSONException
    {
      if (obj.has(key)) { return obj.getBoolean(key); }
      return def;
    }
    
    // Check first to ensure, no exception will be thrown.
    public int getInt(String key, int def) throws JSONException
    {
      if (obj.has(key)) { return obj.getInt(key); }
      return def;
    }
    
    // Check first to ensure, no exception will be thrown.
    public double getDouble(String key, double def) throws JSONException
    {
      if (obj.has(key)) { return obj.getDouble(key); }
      return def;
    }
    
    // Check first to ensure, no exception will be thrown.
    public String getStr(String key, String def) throws JSONException
    {
      if (obj.has(key)) { return obj.getString(key); }
      return def;
    }
    
    @Override
    public String toString()
    {
      return obj.toString();
    }
}
