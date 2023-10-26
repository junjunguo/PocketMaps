package com.junjunguo.pocketmaps.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.oscim.utils.IOUtils;

/**
 * Hints from: https://medium.com/@svaish97/sending-and-receiving-data-via-bluetooth-android-3b4a44406e84
 * @author Paul Kashofer soundmodul@gmx.at
 */
public class BluetoothUtil
{
  private final static Logger log = Logger.getLogger(BluetoothUtil.class.getName());
  public final static int MSG_FINISH = 1000;
  public final static int MSG_PROGRESS = 50;
  public final static int MSG_STARTED = 0;
  public final static int MSG_FAILED = -1;
  
  public enum HeaderType { File }
  
  BluetoothService service;
  
  public static String PERMISSIONS[] = {
    "android.permission.BLUETOOTH_CONNECT",
    "android.permission.BLUETOOTH_ADVERTISE",
    "android.permission.BLUETOOTH_SCAN",
    "android.permission.BLUETOOTH_ADMIN"
  };
  
  public BluetoothUtil(UUID uuid, String name, Activity activity)
  {
    service = new BluetoothService(activity, name, uuid);
  }
  
  public boolean isSupported()
  {
    return service.isSupported();
  }
  
  public boolean isEnabled()
  {
    return service.isEnabled();
  }
  
  public static void requestPermission(Activity activity)
  {
    if (android.os.Build.VERSION.SDK_INT >= 31) // android.os.Build.VERSION_CODES.S
    {
      //sPermission[0] = android.Manifest.permission.BLUETOOTH_CONNECT;
      androidx.core.app.ActivityCompat.requestPermissions(activity,
                              PERMISSIONS,
                              getId());
    }
  }

  public static boolean isPermissionAllowed(Activity activity)
  {
    if (android.os.Build.VERSION.SDK_INT >= 31) // android.os.Build.VERSION_CODES.S
    {
      for (String p : PERMISSIONS)
      {
        if (androidx.core.content.ContextCompat.checkSelfPermission(activity, p) == android.content.pm.PackageManager.PERMISSION_DENIED) { return false; }
      }
    }
    return true; // Not necessary on older versions
  }

  private static int curId = 0;
  public static int getId()
  {
    curId++;
    return curId;
  }
  
  
  public void requestEnable(Activity activity)
  {
    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    activity.startActivityForResult(enableIntent, 0);
  }
  
  public boolean isConnected()
  {
    return service.getState() == BluetoothService.STATE_CONNECTED;
  }
  
  public void startReceiver(Handler receiver)
  {
    service.startReceiver(receiver);
  }
  
  int selectedIndex = -1;
  /**
   * Start the ConnectThread to initiate a connection to a remote device.
   *
   * @param context The Activity context.
   * @param msgHandler The handler that sends empty messages of BluetoothUtil.MSG_XXX.
   */
  public void connect(Context context, Handler msgHandler)
  {
    Properties devices = service.getPairedDevices();
    final String choices[] = new String[devices.size()];
    int cnt = 0;
    for (Object entry : service.getPairedDevices().entrySet())
    {
      choices[cnt] = entry.toString();
      cnt++;
    }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("BT devices")
                .setPositiveButton("Select", (dialog, which) -> {
                    int idx = choices[selectedIndex].lastIndexOf("=");
                    if (idx < 0) { log.warning("Device list error."); }
                    else
                    {
                      String addr = choices[selectedIndex].substring(idx+1);
                      service.connect(addr, msgHandler);
                    }
                }).setSingleChoiceItems(choices, 0, (dialog, which) -> { selectedIndex = which; });
        AlertDialog dialog = builder.create();
        dialog.show();

  }
  
  /** Transmits a file.
   * @param file The file to transmit.
   * @param msgHandler The handler that returns the progress in percent, or MSG_FAILED, or MSG_FINISH. */
  public void transmit(File file, Handler msgHandler)
  {
    if (Looper.getMainLooper().getThread() == Thread.currentThread())
    {
      new Thread(() -> transmitNow(file, msgHandler)).start();
    }
    else
    {
      transmitNow(file, msgHandler);
    }
  }
  
  private void transmitNow(File file, Handler msgHandler)
  {
    long fsize = file.length();
    long fwritten = 0;
    try (FileInputStream fis = new FileInputStream(file))
    {
      byte buffer[] = new byte[1024*4];
      String msgS = createHeader(HeaderType.File, file.length(), file.getName());
      if (!service.write(msgS)) { throw new IOException("Writing-header-error."); }
      while(true)
      {
        int len = fis.read(buffer);
        if (len < 0) { break; }
        if (!service.write(buffer, len)) { throw new IOException("Writing-data-error."); }
        fwritten += len;
        float percent = ((float)fwritten/fsize) * 100.0f;
        Message msg = new Message();
        msg.what = MSG_PROGRESS;
        msg.arg1 = (int)percent;
        msgHandler.sendMessage(msg);
      }
      msgHandler.sendEmptyMessage(MSG_FINISH);
    }
    catch(IOException e)
    {
      msgHandler.sendEmptyMessage(MSG_FAILED);
      e.printStackTrace();
    }
  }
  
  public static Handler createFileReceiver(File fileDir, TextView view)
  {
    Handler h = new Handler()
    {
      boolean writeError = false;
      int counter = 0;
      FileOutputStream fos;
      @Override public void handleMessage(Message msg)
      {
        if (writeError) { return; }
        if (msg.what == BluetoothUtil.MSG_FAILED)
        {
          IOUtils.closeQuietly(fos);
          view.setText("Receiving failed, try again");
          view.setTextColor(0xFFFF0000); // 0xAARRGGBB
        }
        else if (msg.what == BluetoothUtil.MSG_STARTED)
        {
          view.setText("Receiving started ...");
          view.setTextColor(0xFFFF0000); // 0xAARRGGBB
          String fileName = headerGetText(msg.obj.toString(), 2);

          log.log(Level.INFO, "Writing Bluetooth data to {0}", fileName);
          if (fileName == null)
          {
            view.setText("Error reading header");
            view.setTextColor(0xFFFF0000); // 0xAARRGGBB
            writeError = true;
          }
          else if (fos != null)
          {
            view.setText("Error writing file, stream already created");
            view.setTextColor(0xFFFF0000); // 0xAARRGGBB
            writeError = true;
          }
          else
          {
            log.log(Level.INFO, "Start receiving file: {0}", fileName);
            try
            {
              fos = new FileOutputStream(new File(fileDir, fileName), false);
            }
            catch (IOException e)
            {
              e.printStackTrace();
              view.setText("Error crating file-stream");
              view.setTextColor(0xFFFF0000); // 0xAARRGGBB
              writeError = true;
            }
          }
        }
        else if (msg.what == BluetoothUtil.MSG_PROGRESS)
        {
          view.setText("Receiving [" + counter + "][" + msg.arg1 + "%]");
          view.setTextColor(0xFF0000FF); // 0xAARRGGBB
          try
          {
            fos.write((byte[])msg.obj, 0, msg.arg2);
          }
          catch (IOException e)
          {
            e.printStackTrace();
            view.setText("Error writing file");
            view.setTextColor(0xFFFF0000); // 0xAARRGGBB
            writeError = true;
          }
          counter ++;
        }
        else if (msg.what == BluetoothUtil.MSG_FINISH)
        {
          IOUtils.closeQuietly(fos);
          view.setText("File successfully received");
          view.setTextColor(0xFF00FF00); // 0xAARRGGBB
        }
      }
    };
    return h;
  }
  
  protected static String createHeader(HeaderType type, long dataLength, String ...more)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(type.toString()).append(":").append(dataLength);
    for (String m : more) { sb.append(":").append(m); }
    return sb.toString();
  }
  
  protected static HeaderType headerGetType(String headText)
  {
    String typeS = headerGetText(headText, 0);
    if (HeaderType.File.toString().equals(typeS))
    {
      return HeaderType.File;
    }
    return null;
  }
  
  protected static long headerGetDataLength(String headText)
  {
    String lenS = headerGetText(headText, 1);
    if (lenS == null) { return -1; }
    try
    {
      return Long.parseLong(lenS);
    }
    catch (NumberFormatException e)
    {
      e.printStackTrace();
      return -1;
    }
  }

  /** Returns text from header.
   * @param headText the whole headText.
   * @param num The array-number, where 0=HeaderType and 1=DataLength  
   * @return The requested text. */  
  protected static String headerGetText(String headText, int num)
  {
    String arr[] = headText.split(":");
    if (arr.length <= num) { return null; }
    return arr[num];
  }
}
