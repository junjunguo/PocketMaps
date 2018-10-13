package com.junjunguo.pocketmaps.downloader;

import com.junjunguo.pocketmaps.activities.MainActivity;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
    updateNotification("PocketMaps", txt, false);
  }

  private void updateNotification(String title, String text, boolean ongoing)
  {
    PendingIntent contentIntent = PendingIntent.getActivity(c, 0,
                    new Intent(c, MainActivity.class),   PendingIntent.FLAG_UPDATE_CURRENT);
    Notification.Builder mBuilder = new Notification.Builder(c).setSmallIcon(R.drawable.ic_dialog_info)
                    .setContentTitle(title).setContentText(text).setContentIntent(contentIntent).setOngoing(ongoing);
    NotificationManager notificationManager = (NotificationManager) c.getSystemService(c.NOTIFICATION_SERVICE);
    notificationManager.notify(id, mBuilder.build());
  }
}
