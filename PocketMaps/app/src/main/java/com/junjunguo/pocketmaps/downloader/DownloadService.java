package com.junjunguo.pocketmaps.downloader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.activities.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadService extends Service {
    public static final String ACTION_START = "com.junjunguo.pocketmaps.START_DOWNLOAD";
    public static final String ACTION_STOP = "com.junjunguo.pocketmaps.STOP_DOWNLOAD";
    public static final String EXTRA_MAP_NAME = "map_name";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_FILE_LENGTH = "file_length";
    
    private static final ConcurrentHashMap<String, DownloadInfo> activeDownloads = new ConcurrentHashMap<>();
    private NotificationManager notificationManager;
    private boolean isRunning = false;
    
    public static class DownloadInfo {
        public String mapName;
        public String url;
        public long fileLength;
        public long downloaded;
        public int notificationId;
        public Thread downloadThread;
        public boolean cancelled;
        
        public DownloadInfo(String mapName, String url, long fileLength, int notificationId) {
            this.mapName = mapName;
            this.url = url;
            this.fileLength = fileLength;
            this.downloaded = 0;
            this.notificationId = notificationId;
            this.cancelled = false;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        
        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            String mapName = intent.getStringExtra(EXTRA_MAP_NAME);
            String url = intent.getStringExtra(EXTRA_URL);
            long fileLength = intent.getLongExtra(EXTRA_FILE_LENGTH, 0);
            int notificationId = mapName.hashCode();
            
            DownloadInfo info = new DownloadInfo(mapName, url, fileLength, notificationId);
            activeDownloads.put(mapName, info);
            
            startForeground(notificationId, createNotification(mapName, 0));
            startDownload(info);
            isRunning = true;
        } else if (ACTION_STOP.equals(action)) {
            String mapName = intent.getStringExtra(EXTRA_MAP_NAME);
            stopDownload(mapName);
        }
        
        return START_NOT_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        for (DownloadInfo info : activeDownloads.values()) {
            info.cancelled = true;
            if (info.downloadThread != null) {
                info.downloadThread.interrupt();
            }
        }
        activeDownloads.clear();
        isRunning = false;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("download_channel", "Downloads", NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification(String mapName, int percent) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, "download_channel");
        } else {
            builder = new Notification.Builder(this);
        }
        
        return builder.setContentTitle("Downloading")
                .setContentText(mapName + ": " + percent + "%")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setProgress(100, percent, false)
                .build();
    }
    
    private void startDownload(final DownloadInfo info) {
        info.downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File dlDir = new File(getFilesDir(), "downloads");
                    if (!dlDir.exists()) dlDir.mkdirs();
                    File downloadFile = new File(dlDir, info.mapName + ".ghz");
                    
                    URL url = new URL(info.url);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    FileOutputStream fos = new FileOutputStream(downloadFile);
                    byte[] buffer = new byte[8192];
                    int count;
                    long lastUpdate = System.currentTimeMillis();
                    
                    while ((count = is.read(buffer)) != -1 && !info.cancelled) {
                        info.downloaded += count;
                        fos.write(buffer, 0, count);
                        
                        if (System.currentTimeMillis() - lastUpdate > 500 && info.fileLength > 0) {
                            int percent = (int) (info.downloaded * 100 / info.fileLength);
                            updateNotification(info, percent);
                            lastUpdate = System.currentTimeMillis();
                        }
                    }
                    
                    fos.close();
                    is.close();
                    conn.disconnect();
                    
                    if (!info.cancelled) {
                        notificationManager.notify(info.notificationId,
                                createFinishNotification(info.mapName));
                    }
                } catch (Exception e) {
                    if (!info.cancelled) {
                        e.printStackTrace();
                        notificationManager.notify(info.notificationId,
                                createErrorNotification(info.mapName));
                    }
                } finally {
                    activeDownloads.remove(info.mapName);
                    if (activeDownloads.isEmpty()) {
                        stopSelf();
                    }
                }
            }
        });
        info.downloadThread.start();
    }
    
    private void updateNotification(DownloadInfo info, int percent) {
        notificationManager.notify(info.notificationId, createNotification(info.mapName, percent));
    }
    
    private Notification createFinishNotification(String mapName) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, "download_channel");
        } else {
            builder = new Notification.Builder(this);
        }
        
        return builder.setContentTitle("Download Complete")
                .setContentText(mapName + " downloaded")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }
    
    private Notification createErrorNotification(String mapName) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, "download_channel");
        } else {
            builder = new Notification.Builder(this);
        }
        
        return builder.setContentTitle("Download Failed")
                .setContentText(mapName + " download failed")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }
    
    private void stopDownload(String mapName) {
        DownloadInfo info = activeDownloads.get(mapName);
        if (info != null) {
            info.cancelled = true;
            if (info.downloadThread != null) {
                info.downloadThread.interrupt();
            }
            activeDownloads.remove(mapName);
            notificationManager.cancel(info.notificationId);
        }
        if (activeDownloads.isEmpty()) {
            stopSelf();
        }
    }
    
    public static DownloadInfo getDownloadInfo(String mapName) {
        return activeDownloads.get(mapName);
    }
}
