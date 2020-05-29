package com.junjunguo.pocketmaps.downloader;

import com.junjunguo.pocketmaps.activities.MainActivity;

import android.R;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;

public class ProgressPublisher
{
  Context c;
  int id;
  int notifyIntervalMS = 1000;
  long lastTime = System.currentTimeMillis();
  
  public ProgressPublisher(Context c)
  {
    this.c = c;
    this.id = (int)(Math.random() * 100000.0);
  }
  
  public ProgressPublisher(Context c, int id)
  {
    this.c = c;
    this.id = id;
  }
  
  /** Set the minimum time for the next update notification. **/
  public void setInterval(int notifyIntervalMS)
  {
    this.notifyIntervalMS = notifyIntervalMS;
  }
  
  /** Create message, or update text of message.
   *  @param checkTime Check the interval time, update only when ok.
   *  @param percent Will be appended to message. **/
  public void updateText(boolean checkTime, String txt, int percent)
  {
    if (checkTime)
    {
      long curTime = System.currentTimeMillis();
      long diffTime = curTime - lastTime;
      if (diffTime < notifyIntervalMS) { return; }
      lastTime = curTime;
    }
    updateNotification("PocketMaps", txt + ": " + percent + "%", true);
  }
  
  public void updateTextFinal(String txt)
  {
    if (Build.VERSION.SDK_INT >= 26) //OREO
    {
      updateTextFinalOrio(txt);
    }
    else
    {
      updateTextFinalInternal(txt);
    }
  }
  
  private void updateTextFinalInternal(String txt)
  {
    updateNotification("PocketMaps", txt, false);
  }

  private void updateNotification(String title, String text, boolean ongoing)
  {
    PendingIntent contentIntent = PendingIntent.getActivity(c, 0,
                    new Intent(c, MainActivity.class),   PendingIntent.FLAG_UPDATE_CURRENT);
    NotificationManager nMgr = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
    Notification.Builder mBuilder = createNotification(c, nMgr).setSmallIcon(R.drawable.ic_dialog_info)
                    .setContentTitle(title).setContentText(text).setContentIntent(contentIntent).setOngoing(ongoing);
    nMgr.notify(id, mBuilder.build());
  }
  
  @SuppressWarnings("deprecation")
  private Notification.Builder createNotification(Context c, NotificationManager nMgr)
  {
    if (Build.VERSION.SDK_INT >= 26) //OREO
    {
      String sid = ProgressPublisher.class.getName();
      NotificationChannel channel = new NotificationChannel(sid, "Pocketmaps", NotificationManager.IMPORTANCE_LOW);
      nMgr.createNotificationChannel(channel);
      return createNotificationOreo(c, sid);
    }
    return new Notification.Builder(c);
  }
  
  @TargetApi(26) //OREO
  private void updateTextFinalOrio(String txt)
  { // Crazy: Android is blocking when too much notification updates, so final message does not receive. Try later :(
    int sleepTime = 300;
    for (int i=0; i<10; i++)
    {
      updateTextFinalInternal(txt);
      NotificationManager nMgr = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
      boolean found = false;
      for (StatusBarNotification n : nMgr.getActiveNotifications())
      {
        if (n.getId() != id) { continue; }
        found = true;
        if (!n.isOngoing()) { return; }
      }
      if (!found) { return; }
      try { Thread.sleep(sleepTime); } catch (Exception e) {}
      sleepTime = (sleepTime/2) * 3;
    }
  }
  
  @TargetApi(26) //OREO
  private Notification.Builder createNotificationOreo(Context c, String sid)
  {
    return new Notification.Builder(c, sid);
  }
}
