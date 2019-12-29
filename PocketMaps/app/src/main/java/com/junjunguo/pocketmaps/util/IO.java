package com.junjunguo.pocketmaps.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.activities.MainActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Environment;

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
  
  /** From Android 8 (Orio) there is a bug for downloading to sd-card.
   * <br/>In this case use an internal storage for download.
   * @param requestedDir The external directory that is desired. */
  public static File getDownloadDirectory(File requestedDir)
  {
    if (Build.VERSION.SDK_INT >= 26) // OREO
    { // We just assume Download-Dir is mounted.
      return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }
    return requestedDir;
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
    final ArrayList<String> items = new ArrayList<String>();
    final ArrayList<String> itemsText = new ArrayList<String>();
    itemsText.add(getStorageText(MainActivity.getDefaultBaseDirectory(activity)));
    items.add(MainActivity.getDefaultBaseDirectory(activity).getPath());
    int curSelected = 0;
    String curFolder = Variable.getVariable().getMapsFolder().getPath();
    for (File curFile : files)
    {
      if (curFile == null)
      { // Regarding to android javadoc this may be possible.
        continue;
      }
      String mountState = Environment.getExternalStorageState(curFile);
      if (mountState.equals(Environment.MEDIA_MOUNTED) ||
          mountState.equals(Environment.MEDIA_SHARED))
      {
        itemsText.add(getStorageText(curFile));
        items.add(curFile.getPath());
        if (curFolder.startsWith(curFile.getPath()))
        {
          curSelected = items.size() - 1;
        }
      }
      
    }
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        String selection = items.get(buttonNr);
        Variable.getVariable().setBaseFolder(selection);
        File mapsFolder = Variable.getVariable().getMapsFolder();
        if (!mapsFolder.exists()) { mapsFolder.mkdirs(); }
        Variable.getVariable().saveVariables(Variable.VarType.Base);
        Variable.getVariable().getLocalMaps().clear();
        dialog.dismiss();
        callback.run();
      }
    };
    builder1.setSingleChoiceItems(itemsText.toArray(new String[0]), curSelected, listener);
  }

  private static String getStorageText(File dir)
  {
    long freeSpace = dir.getFreeSpace() / (1024*1024);
    long totalSpace = dir.getTotalSpace() / (1024*1024);
    return "[ " + freeSpace + " MB free / " + totalSpace + " MB total]\n" + dir.getPath() + "\n";
  }
}
