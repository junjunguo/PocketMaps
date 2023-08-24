package com.junjunguo.pocketmaps.downloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.util.Variable;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.junjunguo.pocketmaps.activities.ExportActivity;
import com.junjunguo.pocketmaps.activities.GeocodeActivity;
import java.io.OutputStream;
import org.oscim.utils.IOUtils;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on September 19, 2015.
 */
public class MapUnzip {
    public static final int ANDROID_API_MARSHMALLOW = Build.VERSION_CODES.LOLLIPOP + 2;
    public static final int BUFFER_SIZE = 8 * 1024;

    /** Unzips a map from the zip-path, and shows progress.
     * @param zipFilePath The zip path
     * @param mapName The map name to unzip
     * @param pp The ProgressPublisher to show progress.
     * @throws java.io.IOException */
    public void unzip(String zipFilePath, String mapName, ProgressPublisher pp) throws IOException
    {
      ZipInputStream zipIn = null;
      try{
        File mapFolder = new File(Variable.getVariable().getMapsFolder(), mapName + "-gh");
        File destDir = new File(mapFolder.getAbsolutePath());
        if (destDir.exists()) {
            recursiveDelete(destDir);
        }
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        int up = 0;
        // iterates over entries in the zip file
        while (entry != null) {
            up++;
            long fSize = entry.getSize();
            pp.updateText(false, "" + up + " Unzipping " + mapName, 0);
            String mapFileName = new File(entry.getName()).getName();
            String filePath = mapFolder.getAbsolutePath() + File.separator + mapFileName;
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath, pp, "" + up + " Unzipping " + mapName, fSize, null);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
      }
      finally
      {
        IOUtils.closeQuietly(zipIn);
      }
    }
    
    /** Unzip an exported-file (.pmz) and import it.
   * @param context The Android context
   * @param zipFilePath The zip path
   * @return True when successful. **/
    public boolean unzipImport(final String zipFilePath, final Context context)
    {
      ZipInputStream zipIn = null;
      try
      {
        zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        if (entry==null) { return false; }
        if (ExportActivity.getFileType(entry.getName()) == ExportActivity.FileType.Map)
        { // Is a map file!
          String mapName = entry.getName().substring("/maps/".length());
          int index = mapName.indexOf("-gh/");
          if (index <= 0) { return false; }
          mapName = mapName.substring(0,index);
          final String mapNameFinal = mapName;
          
          //TODO: RefreshMapList, or use DownloadMapActivity.createStatusUpdater() --> BroadcastReceiver
          Thread t = new Thread(() ->
          { // Because this may be a long running task, we dont use AsyncTask.
            ProgressPublisher pp = new ProgressPublisher(context);
            pp.updateText(false, "Unzipping " + mapNameFinal, 0);
            String finishTxt = "Finish: ";
            try
            {
              unzip(zipFilePath, mapNameFinal, pp);
            }
            catch (IOException e) { e.printStackTrace(); finishTxt = "Unzip error: "; }
            pp.updateTextFinal(finishTxt + mapNameFinal);
            
            MyMap myMap = new MyMap(mapNameFinal);
            Variable.getVariable().getRecentDownloadedMaps().add(myMap);
            myMap.setStatus(MyMap.DlStatus.Complete);
          });
          t.start();
          return true;
        }
        boolean reSettings = false;
        boolean reFav = false;
        boolean reTrac = false;
        while (entry != null)
        {
          if (ExportActivity.getFileType(entry.getName()) == ExportActivity.FileType.Setting)
          {
            extractFile(zipIn, entry.getName(), null, "", 0, context);
            reSettings = true;
          }
          else if (ExportActivity.getFileType(entry.getName()) == ExportActivity.FileType.Favourites)
          {
            String favFolder = Variable.getVariable().getMapsFolder().getParent();
            String favFile = new File(favFolder, "Favourites.properties").getPath();
            extractFile(zipIn, favFile, null, "", 0, null);
            GeocodeActivity.resetFavourites();
            reFav = true;
          }
          else if (ExportActivity.getFileType(entry.getName()) == ExportActivity.FileType.Tracking)
          {
            File tracFolder = Variable.getVariable().getTrackingFolder();
            String tracFile = new File(tracFolder, new File(entry.getName()).getName()).getPath();
            extractFile(zipIn, tracFile, null, "", 0, null);
            reTrac = true;
          }
          entry = zipIn.getNextEntry();
        }
        String reSettingsS = "Loaded: ";
        if (reSettings)
        {
          for (Variable.VarType vt : Variable.VarType.values())
          {
            Variable.getVariable().loadVariables(vt);
          }
          reSettingsS += "[Settings] ";
        }
        if (reFav)
        {
          reSettingsS += "[Favourites] ";
        }
        if (reTrac)
        {
          reSettingsS += "[Tracking-recs] ";
        }
        ProgressPublisher pp = new ProgressPublisher(context);
        pp.updateTextFinal(reSettingsS);
      }
      catch (IOException e) { e.printStackTrace(); return false; }
      finally
      {
        IOUtils.closeQuietly(zipIn);
      }
      return true;
    }
    
    /** Compress files.
      * @param context If any srcFile is internal, use this context. 
      * @param pp ProgressPublisher may be null.
      * @param tarZipFile Target file **/
    public boolean compressFiles(ArrayList<String> srcFiles, ArrayList<String> zipSubDirs, String tarZipFile, ProgressPublisher pp, Context context)
    {
      if (srcFiles.isEmpty()) { return true; }
      ZipOutputStream zout = null;
      FileInputStream fis = null;
      try
      {
        zout = new ZipOutputStream(new FileOutputStream(tarZipFile));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int fcount = 0;
        int flen = srcFiles.size();
        for (int i=0; i<srcFiles.size(); i++)
        {
          String curFile = srcFiles.get(i);
          String zipSubDir = zipSubDirs.get(i);
          String zipName = new File(curFile).getName();
          if (!zipSubDir.isEmpty()) { zipName = new File(zipSubDir, zipName).getPath(); }
          if (pp!=null) { fcount++; pp.updateText(false, "" + fcount + "/" + flen + " Export " + zipName, 0); }
          File f = new File(curFile);
          long bcount = 0;
          long bLen = 4000;
          if (f.exists()) { fis = new FileInputStream(new File(curFile)); bLen = f.length(); }
          else { fis = context.openFileInput(curFile); }
          ZipEntry zipEntry = new ZipEntry(zipName);
          zout.putNextEntry(zipEntry);
          while (true)
          {
            int count = fis.read(bytesIn);
            if (count < 0) { break; }
            if (pp!=null) { int per = (int)((bcount*100)/bLen); pp.updateText(true, "" + fcount + "/" + flen + " Export " + zipName, per); }
            zout.write(bytesIn, 0, count);
          }
          fis.close();
          zout.closeEntry();
        }
        if (pp!=null) { pp.updateTextFinal("Finish: Export " + new File(tarZipFile).getName()); }
      }
      catch (IOException e)
      {
        e.printStackTrace();
        IOUtils.closeQuietly(fis);
        if (pp!=null) { pp.updateTextFinal("Error: Export " + new File(tarZipFile).getName()); }
        return false;
      }
      finally { IOUtils.closeQuietly(fis); IOUtils.closeQuietly(zout); }
      return true;
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn The zip
     * @param filePath Target path, may be internal, when context not null
     * @param pp Show Progress, or may be null
     * @param mapName Map name
     * @param fSize The file size for progress or 0 for 50.
     * @param ppText Text for progress
     * @param appContextWhenInternal When filePath is internal, or null for regular file.
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn,
                             String filePath,
                             ProgressPublisher pp,
                             String ppText,
                             long fSize,
                             Context appContextWhenInternal) throws IOException {
      BufferedOutputStream bos = null;
      try{
        if (appContextWhenInternal==null) { bos = new BufferedOutputStream(new FileOutputStream(filePath)); }
        else { bos = new BufferedOutputStream(appContextWhenInternal.openFileOutput(filePath, Context.MODE_PRIVATE)); }
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        long readCounter = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
            float percent = 50.0f;
            if (fSize>0)
            {
              readCounter += read;
              percent = ((float)readCounter) / ((float)fSize);
              percent = percent * 100.0f;
            }
            if (pp!=null) { pp.updateText(true, ppText, (int)percent); }
        }
      }
      finally
      {
        IOUtils.closeQuietly(bos);
      }
    }

    /**
     * delete a recursively delete a folder or file
     *
     * @param fileOrDirectory
     */
    public void recursiveDelete(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) for (File child : fileOrDirectory.listFiles())
            recursiveDelete(child);
        try {
            fileOrDirectory.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Check when status was updated from ProgressPublisher.java
     *  Should update each second, otherwise the progress seems not alive.
     *  @return False, when status was not updated for 30 sec. **/
    public static boolean checkUnzipAlive(Context c, MyMap myMap)
    {
      if (Build.VERSION.SDK_INT < ANDROID_API_MARSHMALLOW)
      {
        return checkUnzipAliveOldVersion(c, myMap);
      }
      return checkUnzipAliveInternal(c, myMap);
    }
    
    private static boolean checkUnzipAliveOldVersion(Context c, MyMap myMap)
    {
      File mDir = MyMap.getMapFile(myMap, MyMap.MapFileType.MapFolder);
      if (timeCheck(mDir.lastModified())) { return true; }
      File list[] = mDir.listFiles();
      if (list != null)
      {
        for (File f : list)
        {
          if (timeCheck(f.lastModified())) { return true; }
        }
      }
      return false;
    }

    @TargetApi(ANDROID_API_MARSHMALLOW)
    private static boolean checkUnzipAliveInternal(Context c, MyMap myMap)
    {
      String unzipKeyId = myMap.getMapName() + "-unzip";
      NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
      
      for (StatusBarNotification n : notificationManager.getActiveNotifications())
      {
        if (n.getId() == unzipKeyId.hashCode())
        {
          long postTime = n.getPostTime();
          return timeCheck(postTime);
        }
      }
      return false;
    }
    
    private static boolean timeCheck(long lastTime)
    {
      long curTime = System.currentTimeMillis();
      long diffTime = curTime - lastTime;
      if (diffTime > 30000) { return false; }
      return true;
    }
}
