package com.junjunguo.pocketmaps.activities;

import com.junjunguo.pocketmaps.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.junjunguo.pocketmaps.downloader.ProgressPublisher;

public class Permission  extends AppCompatActivity
implements ActivityCompat.OnRequestPermissionsResultCallback, OnClickListener
{
  static String[] sPermission;
  static boolean isFirstForcedPermission;
  static int idCounter = 0;
  static boolean isAsking = false;
  private static final int REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 1001;
  private static final int REQUEST_BACKGROUND_LOCATION = 1002;

  /** Start a Permission-Request, and calls activity.finish().
   *  @param sPermission The Permission of android.Manifest.permission.xyz
   *  @param isFirstForcedPermission True, if the first one of sPermission is forced. **/
  public static void startRequest(String[] sPermission, boolean isFirstForcedPermission, Activity activity)
  {
    Permission.sPermission = sPermission;
    Permission.isFirstForcedPermission = isFirstForcedPermission;

    Intent intent = new Intent(activity, Permission.class);
    activity.startActivity(intent);

    if (isFirstForcedPermission && !checkPermission(sPermission[0], activity))
    { // On new Android (13?) we need to ask for POST_NOTIFICATIONS, and first notification is not shown.
      new ProgressPublisher(activity).updateTextFinal("Welcome to PocketMaps");
    }
  }

  /** Request background location permission for Android 10+ **/
  public static void requestBackgroundLocationPermission(Activity activity)
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
    {
      if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) 
          != PackageManager.PERMISSION_GRANTED)
      {
        new AlertDialog.Builder(activity)
          .setTitle("Background Location Required")
          .setMessage("To track your location while navigating in the background, please grant 'Allow all the time' location permission.")
          .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                REQUEST_BACKGROUND_LOCATION);
            }
          })
          .setNegativeButton("Not Now", null)
          .show();
      }
    }
  }

  /** Request battery optimization exemption for Android 6+ **/
  @SuppressLint("BatteryLife")
  public static void requestBatteryOptimizationExemption(Activity activity)
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
      if (pm != null && !pm.isIgnoringBatteryOptimizations(activity.getPackageName()))
      {
        new AlertDialog.Builder(activity)
          .setTitle("Battery Optimization")
          .setMessage("For reliable location tracking, please disable battery optimization for PocketMaps.")
          .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
              intent.setData(Uri.parse("package:" + activity.getPackageName()));
              activity.startActivityForResult(intent, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            }
          })
          .setNegativeButton("Not Now", null)
          .show();
      }
    }
  }

  /** Check if background location permission is granted **/
  public static boolean hasBackgroundLocationPermission(Context context)
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
    {
      return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) 
          == PackageManager.PERMISSION_GRANTED;
    }
    return true;
  }

  /** Check if battery optimization is disabled for this app **/
  public static boolean isBatteryOptimizationDisabled(Context context)
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
      return pm != null && pm.isIgnoringBatteryOptimizations(context.getPackageName());
    }
    return true;
  }
  
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (sPermission == null) { finish(); return; }
    setContentView(R.layout.activity_text);
    Button okButton = (Button) findViewById(R.id.okTextButton);
    EditText listText = (EditText) findViewById(R.id.areaText);
    listText.setFocusable(false);
    listText.setText(getPermissionText());
    okButton.setOnClickListener(this);
  }
  
  private CharSequence getPermissionText()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Asking for permissions:\n\n");
    String necessary = "Necessary:\n";
    if (!isFirstForcedPermission) { necessary = "Recommended:\n"; }
    for (String curPermission : sPermission)
    {
      sb.append(necessary);
      necessary = "Recommended:\n";
      sb.append(curPermission.replace('.', '\n'));
      sb.append("\n\n");
    }
    return sb;
  }

  @Override protected void onResume()
  {
    super.onResume();
    if (!isAsking) {}
    else if (checkPermission(sPermission[0], this))
    {
      finish();
    }
    else if (!isFirstForcedPermission)
    {
      finish();
    }
    else
    {
      logUser("App needs access!!!");
    }
    isAsking = false;
  }
  
  @Override
  public void onClick(View v)
  {
    if (v.getId()==R.id.okTextButton)
    {
      log("Selected: Permission-Ok");
      requestPermissionLater(this, sPermission);
      isAsking = true;
    }
  }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_BACKGROUND_LOCATION)
        {
          if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
          {
            logUser("Background location permission granted!");
          }
          else
          {
            logUser("Background location permission denied - tracking may be limited");
          }
          finish();
          return;
        }
            if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                finish();
            } else {
              if (!isFirstForcedPermission)
              {
                finish();
              }
              else
              {
                logUser("App needs access for this feature!");
              }
            }
    }

    /** Check if permission is already permitted.
     *  @param sPermission The Permission of android.Manifest.permission.xyz **/
    public static boolean checkPermission(String sPermission, Context context) {
        // Check if the Camera permission has been granted
        if (ActivityCompat.checkSelfPermission(context, sPermission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    
    /** Check for permission to permit.
     * @param activity The parent activity.
     * @param sPermission The Permission of android.Manifest.permission.xyz **/
    public static void requestPermissionLater(Activity activity, String[] sPermission) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                sPermission)) {
                    ActivityCompat.requestPermissions(activity,
                            sPermission,
                            getId());
//        } else {
//          logUser("Permission is not available: " + sPermission);
//          return false;
//        }
    }
    
    /** Returns true, when all permissions are allowed.
    * @param activity The parent activity.
    * @param sPermission All permissions to request.
    * @return True, when all permissions are allowed. */
    public static boolean getPermissionsAllowed(Activity activity, String[] sPermission)
    {
      for (String p : sPermission)
      {
        if (androidx.core.content.ContextCompat.checkSelfPermission(activity, p) == android.content.pm.PackageManager.PERMISSION_DENIED) { return false; }
      }
      return true;
    }
    
    private static int getId()
    {
      idCounter ++;
      return idCounter;
    }

    private void log(String str)
    {
      Log.i(Permission.class.getName(), str);
    }
    
    private void logUser(String str)
    {
      Log.i(Permission.class.getName(), str);
      try
      {
        Toast.makeText(this.getBaseContext(), str, Toast.LENGTH_SHORT).show();
      }
      catch (Exception e) { e.printStackTrace(); }
    }
}

