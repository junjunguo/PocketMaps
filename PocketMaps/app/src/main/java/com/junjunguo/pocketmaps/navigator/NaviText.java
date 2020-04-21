package com.junjunguo.pocketmaps.navigator;

import android.app.Activity;
import com.junjunguo.pocketmaps.R;

/**
 *
 * @author ppp
 */
public class NaviText
{
  public static String sIn;
  public static String sMeters;
  public static String sFeet;
  public static String sUseRound;
  public static String sLeaveRound;
  public static String sTurnXXX;
  public static String sKeepXXX;
  public static String sLeft;
  public static String sRight;
  public static String sSlightL;
  public static String sSlightR;
  public static String sSharpL;
  public static String sSharpR;
  public static String sWrongDir;
  public static String sOn;
  public static String sOnto;
  public static String sContinue;
  public static String sNavEnd;
  
  public static void initTextList(Activity activity)
  {
    if (sIn != null) { return; }
    sIn = activity.getResources().getString(R.string.navivoice_in);
    sFeet = activity.getResources().getString(R.string.navivoice_feet);
    sMeters = activity.getResources().getString(R.string.navivoice_meters);
    sUseRound = activity.getResources().getString(R.string.navivoice_useround);
    sLeaveRound = activity.getResources().getString(R.string.navivoice_leaveround);
    sTurnXXX = activity.getResources().getString(R.string.navivoice_turnxxx);
    sKeepXXX = activity.getResources().getString(R.string.navivoice_keepxxx);
    sLeft = activity.getResources().getString(R.string.navivoice_left);
    sRight = activity.getResources().getString(R.string.navivoice_right);
    sSlightL = activity.getResources().getString(R.string.navivoice_slightl);
    sSlightR = activity.getResources().getString(R.string.navivoice_slightr);
    sSharpL = activity.getResources().getString(R.string.navivoice_sharpl);
    sSharpR = activity.getResources().getString(R.string.navivoice_sharpr);
    sWrongDir = activity.getResources().getString(R.string.navivoice_wrongdir);
    sOn = activity.getResources().getString(R.string.navivoice_on);
    sOnto = activity.getResources().getString(R.string.navivoice_onto);
    sContinue = activity.getResources().getString(R.string.navivoice_continue);
    sNavEnd = activity.getResources().getString(R.string.navivoice_navend);
  }
}
