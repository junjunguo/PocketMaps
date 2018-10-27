package com.junjunguo.pocketmaps.downloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.util.Variable;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on September 19, 2015.
 */
public class MapUnzip {
    public static final int ANDROID_API_MARSHMALLOW = Build.VERSION_CODES.LOLLIPOP + 2;
    public static final int BUFFER_SIZE = 8 * 1024;

    public void unzip(String zipFilePath, String mapName, ProgressPublisher pp) throws IOException {
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
            String filePath = mapFolder.getAbsolutePath() + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath, pp, "" + up + " Unzipping " + mapName, fSize);
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
        if (zipIn!=null) zipIn.close();
      }
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn
     * @param filePath
     * @param pp 
     * @param mapName 
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn,
                             String filePath,
                             ProgressPublisher pp,
                             String ppText,
                             long fSize) throws IOException {
      BufferedOutputStream bos = null;
      try{
        bos = new BufferedOutputStream(new FileOutputStream(filePath));
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
            pp.updateText(true, ppText, (int)percent);
        }
      }
      finally
      {
        if (bos!=null) bos.close();
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
