package com.junjunguo.pocketmaps.downloader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * This file is part of Pockets Maps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 14, 2015.
 */
public class DownloadFiles {
    private static DownloadFiles downloadFiles;

    private DownloadFiles() {
    }

    public static DownloadFiles getDownloader() {
        if (downloadFiles == null) {
            downloadFiles = new DownloadFiles();
        }
        return downloadFiles;
    }
    
  /**
   * @param mapUrl
   * @return json string
   */
  public String downloadTextfile(String textFileUrl)
  {
    StringBuilder json = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(textFileUrl).openStream())))
    {
      String lineUrl;
      android.util.Log.i(DownloadFiles.class.getName(),"Pre stream dl");
      while ((lineUrl = in.readLine()) != null)
      {
        json.append(lineUrl);
      }
      in.close();
      android.util.Log.i(DownloadFiles.class.getName(),"Close stream dl");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return json.toString();
  }

}
