package com.junjunguo.pocketmaps.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.activities.MainActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.DataSetObserver;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class IO
{
  public static boolean writeToFile(String txt, File file, boolean append)
  {
    try(FileWriter sw = new FileWriter(file, append);
        BufferedWriter bw = new BufferedWriter(sw))
    {
      bw.write(txt);
      bw.flush();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  public static String readFromFile(File file, String lineBreak)
  {
    StringBuilder sb = new StringBuilder();
    try(FileReader sr = new FileReader(file);
        BufferedReader br = new BufferedReader(sr))
    {
      while (true)
      {
        String txt = br.readLine();
        if (txt == null) { break; }
        sb.append(txt).append(lineBreak);
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return null;
    }
    return sb.toString();
  }
  
  public static void showRootfolderSelector(Activity activity, boolean cacheDir, Runnable callback)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      builder1.setTitle(R.string.switch_maps_dir);
      builder1.setCancelable(true);
      addSelection(builder1, cacheDir, callback, activity);
    }
    else
    {
      builder1.setMessage(R.string.needs_vers_lollipop);
      builder1.setCancelable(false);
    }
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static void addSelection(AlertDialog.Builder builder1, boolean cacheDir, final Runnable callback, Activity activity)
  {
    File files[];
    if (cacheDir)
    {
      files = activity.getExternalCacheDirs();
    }
    else
    {
      files = activity.getExternalMediaDirs();
    }
    final String items[] = new String[files.length + 1];
    String itemsText[] = new String[files.length + 1];
    int curPos = 0;
    itemsText[curPos] = getStorageText(MainActivity.getDefaultMapsDirectory(activity));
    items[curPos] = MainActivity.getDefaultMapsDirectory(activity).getPath();
    for (File curFile : files)
    {
      curPos++;
      itemsText[curPos] = getStorageText(curFile);
      items[curPos] = curFile.getPath();
    }
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        String selection = items[buttonNr];
        File mapsFolder = new File(selection, Variable.getVariable().getMapDirectory());
        if (!mapsFolder.exists()) { mapsFolder.mkdirs(); }
        Variable.getVariable().setMapsFolder(mapsFolder);
        Variable.getVariable().saveVariables();
        Variable.getVariable().getLocalMaps().clear();
        callback.run();
      }
    };
    builder1.setItems(itemsText, listener);
  }

  private static String getStorageText(File dir)
  {
    long freeSpace = dir.getFreeSpace() / (1024*1024);
    long totalSpace = dir.getTotalSpace() / (1024*1024);
    return "[ " + freeSpace + " MB free / " + totalSpace + " MB total]\n" + dir.getPath() + "\n";
  }
}
