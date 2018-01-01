package com.junjunguo.pocketmaps.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
}
