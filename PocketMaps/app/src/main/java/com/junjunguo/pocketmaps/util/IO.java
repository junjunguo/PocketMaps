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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

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
  
  /**
   * Return the size of a directory in megabytes
   */
  public static long dirSize(File dir)
  {
    if (!dir.exists()) return 0;

    long result = 0;
    for (File file : dir.listFiles())
    { // Recursive call if it's a directory
      if (file.isDirectory())
      {
        result += dirSize(file);
      }
      else
      {
        result += file.length();
      }
    }
    return result / (1024 * 1024);
  }
  
  /** From Android 8 (Orio) there is a bug for downloading to sd-card.
   * <br/>In this case use an internal storage for download.
   * @param requestedDir The external directory that is desired. */
  public static File getDownloadDirectory(File requestedDir, Context context)
  {
    if (Build.VERSION.SDK_INT >= 26) // OREO
    { // We just assume Download-Dir is mounted.
      return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }
    return requestedDir;
  }

  public static File getDefaultBaseDirectory(Context context)
  {
    if (Build.VERSION.SDK_INT >= 29)
    { // ExternalStoragePublicDirectory Deprecated since android Q
      File target = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
      if (!Environment.getExternalStorageState(target).equals(Environment.MEDIA_MOUNTED))
      {
        Toast.makeText(context, "Pocket Maps is not usable without an external storage!", Toast.LENGTH_SHORT).show();
        return null;
      }
      return target;
    }
    else if (Build.VERSION.SDK_INT >= 19)
    { // greater or equal to Kitkat
      if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
      {
        Toast.makeText(context, "Pocket Maps is not usable without an external storage!", Toast.LENGTH_SHORT).show();
        return null;
      }
      return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }
    else
    {
      return Environment.getExternalStorageDirectory();
    }
  }
  
  /** Lists some paths that are possible to store exports.
   * @param context Android context
   * @param cacheDir Use cacheDir instead of mediaDir
   * @return The paths listed. */
  public static ArrayList<String> listSelectionPaths(Context context, boolean cacheDir)
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      return listSelectionPathsLollipop(context, cacheDir);
    }
    else
    {
      ArrayList<String> list = new ArrayList<String>();
      list.add(getDefaultBaseDirectory(context).getPath());
      return list;
    }
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
    String curFolder = Variable.getVariable().getMapsFolder().getPath();
    int curSelected = 0;
    final ArrayList<String> items = listSelectionPathsLollipop(activity, cacheDir);
    final ArrayList<String> itemsText = new ArrayList<String>();
    for (String item : items)
    {
      itemsText.add(getStorageText(new File(item)));
      if (curFolder.startsWith(item))
      {
        curSelected = items.indexOf(item);
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

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static ArrayList<String> listSelectionPathsLollipop(Context context, boolean cacheDir)
  {
    File files[];
    if (cacheDir)
    {
      files = context.getExternalCacheDirs();
    }
    else
    {
      files = context.getExternalMediaDirs();
    }
    final ArrayList<String> items = new ArrayList<String>();
    items.add(getDefaultBaseDirectory(context).getPath());
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
        items.add(curFile.getPath());
      }
    }
    return items;
  }
  
  private static String getStorageText(File dir)
  {
    long freeSpace = dir.getFreeSpace() / (1024*1024);
    long totalSpace = dir.getTotalSpace() / (1024*1024);
    return "[ " + freeSpace + " MB free / " + totalSpace + " MB total]\n" + dir.getPath() + "\n";
  }
}
