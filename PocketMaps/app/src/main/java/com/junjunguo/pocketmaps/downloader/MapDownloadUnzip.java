package com.junjunguo.pocketmaps.downloader;

import java.io.File;
import java.io.IOException;

import com.junjunguo.pocketmaps.activities.DownloadMapActivity;
import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.util.IO;
import com.junjunguo.pocketmaps.util.Variable;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class MapDownloadUnzip
{
  public static void checkMap(Activity activity, MyMap tmpMap, StatusUpdate stUpdate)
  {
    File idFile = MyMap.getMapFile(tmpMap, MyMap.MapFileType.DlIdFile);
    if (!idFile.exists())
    {
      stUpdate.logUserThread("Unzipping: " + tmpMap.getMapName());
      unzipBg(activity, tmpMap, stUpdate);
      return;
    }
    String idFileContent = IO.readFromFile(idFile, "\n");
    if (idFileContent.startsWith("" + MyMap.DlStatus.Error + ": "))
    {
      stUpdate.logUserThread(idFileContent);
      DownloadMapActivity.clearDlFile(tmpMap);
    }
    else
    {
      int id = Integer.parseInt(idFileContent.replace("\n", ""));
      broadcastReceiverCheck(activity, tmpMap, stUpdate, id);
    }
  }
  

  public static void unzipBg(final Activity activity, final MyMap myMap, final StatusUpdate stUpdate)
  {
    if (MapUnzip.checkUnzipAlive(activity.getApplicationContext(), myMap))
    {
      log("Unzip is still in progress. Dont start twice.");
      return;
    }
    log("Unzipping map: " + myMap.getMapName());
    myMap.setStatus(MyMap.DlStatus.Unzipping);
    stUpdate.updateMapStatus(myMap);
    Thread t = new Thread(new Runnable(){ public void run()
    { // Because this may be a long running task, we dont use AsyncTask.
      String errMsg = null;
      Context c = activity.getApplicationContext();
      String unzipKeyId = myMap.getMapName() + "-unzip";
      ProgressPublisher pp = new ProgressPublisher(c, unzipKeyId.hashCode());
      pp.updateText(false, "Unzipping " + myMap.getMapName(), 0);
      File ghzFile = MyMap.getMapFile(myMap, MyMap.MapFileType.DlMapFile);
      if (ghzFile.exists())
      {
        try
        {
          new MapUnzip().unzip(ghzFile.getPath(), myMap.getMapName(), pp);
        }
        catch (IOException e)
        {
          errMsg = "Error unpacking map: " + myMap.getMapName();
        }
      }
      else
      {
        errMsg = "Error, missing downloaded file: " + ghzFile.getPath();
      }
      if (errMsg!=null)
      {
        pp.updateTextFinal(errMsg);
      }
      else
      {
        pp.updateTextFinal("Extracting finished: " + myMap.getMapName());
      }
      final String errMsgFinal = errMsg;
      DownloadMapActivity.clearDlFile(myMap);
      activity.runOnUiThread(new Runnable() { public void run()
      {
        if (errMsgFinal!=null)
        {
          File idFile = MyMap.getMapFile(myMap, MyMap.MapFileType.DlIdFile);
          IO.writeToFile("" + MyMap.DlStatus.Error + ": " + errMsgFinal, idFile, false);
          myMap.setStatus(MyMap.DlStatus.Error);
          stUpdate.updateMapStatus(myMap);
          return;
        }
        Variable.getVariable().getRecentDownloadedMaps().add(myMap);
        MyMap.setVersionCompatible(myMap.getMapName(), myMap);
        myMap.setStatus(MyMap.DlStatus.Complete);
        stUpdate.updateMapStatus(myMap);
      } });
    } });
    t.start();
    
  }

  /** Check first, if map is finished, or on pending status register receiver. **/
  private static void broadcastReceiverCheck(Activity activity,
                                       final MyMap myMap,
                                       final StatusUpdate stUpdate,
                                       final long enqueueId)
  {
    int preStatus = getDownloadStatus(activity, enqueueId);
    if (preStatus == DownloadManager.STATUS_SUCCESSFUL)
    {
      stUpdate.logUserThread("Unzipping: " + myMap.getMapName());
      unzipBg(activity, myMap, stUpdate);
      return;
    }
    else if (preStatus == DownloadManager.STATUS_FAILED)
    {
      DownloadMapActivity.clearDlFile(myMap);
      stUpdate.logUserThread("Error post-downloading map: " + myMap.getMapName());
    }
    else
    {
      stUpdate.onRegisterBroadcastReceiver(activity, myMap, enqueueId);
    }
  }
  
  public static int getDownloadStatus(Context context, long enqueueId)
  {
    Query query = new Query();
    DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    query.setFilterById(enqueueId);
    Cursor c = dm.query(query);
    if (c.moveToFirst())
    {
      int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
      return c.getInt(columnIndex);
    }
    return -1; // Aborted.
  }
  
  public static interface StatusUpdate
  {
    public void logUserThread(String txt);
    public void updateMapStatus(MyMap map);
    public void onRegisterBroadcastReceiver(Activity activity, MyMap myMap, long enqueueId);
  }
  
  private static void log(String s) {
    Log.i(MapDownloadUnzip.class.getName(), s);
  }
}
