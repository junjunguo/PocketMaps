package com.junjunguo.pocketmaps.activities;

import com.junjunguo.pocketmaps.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.junjunguo.pocketmaps.bluetooth.BluetoothService;
import com.junjunguo.pocketmaps.bluetooth.BluetoothUtil;
import com.junjunguo.pocketmaps.downloader.MapUnzip;
import com.junjunguo.pocketmaps.downloader.ProgressPublisher;
import com.junjunguo.pocketmaps.util.IO;
import com.junjunguo.pocketmaps.util.Variable;
import com.junjunguo.pocketmaps.bluetooth.Constants;
//import com.junjunguo.pocketmaps.util.BluetoothUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

public class ExportActivity  extends AppCompatActivity implements OnClickListener, OnItemSelectedListener, OnCheckedChangeListener
{
    public enum FileType { Tracking, Favourites, Setting, Map, Unknown }
    public enum EType { Export, Import, Transmit, Receive }
    BluetoothUtil btUtil;
    Spinner exSpinner;
    Spinner exTypeSpinner;
    CheckBox exSetCb;
    CheckBox exFavCb;
    CheckBox exTrackCb;
    CheckBox exMapsCb;
    TextView exFullPathTv;
    TextView exStatus;
    LinearLayout lFList;
    LinearLayout lExport;
    LinearLayout lReceive;
    LinearLayout lMaps;
  
  /** Returns the selected Type. */
  private EType getSelectedType()
  {
    return EType.values()[(int)exTypeSpinner.getSelectedItemId()];
  }
    
  @Override protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_export);
    Button exOk = (Button) findViewById(R.id.exOk);
    Button rcOk = (Button) findViewById(R.id.rcOk);
    exSpinner = (Spinner) findViewById(R.id.exSpinner);
    exTypeSpinner = (Spinner) findViewById(R.id.exTypeSpinner);
    exFullPathTv = (TextView) findViewById(R.id.exFullPathTv);
    exSetCb = (CheckBox) findViewById(R.id.exSet_cb);
    exFavCb = (CheckBox) findViewById(R.id.exFav_cb);
    exTrackCb = (CheckBox) findViewById(R.id.exTrack_cb);
    exMapsCb = (CheckBox) findViewById(R.id.exMaps_cb);
    lFList = (LinearLayout) findViewById(R.id.exLayout_list);
    lExport = (LinearLayout) findViewById(R.id.exLayout_export);
    lReceive = (LinearLayout) findViewById(R.id.exLayout_receive);
    lMaps = (LinearLayout) findViewById(R.id.exLayout_maps);
    exStatus = (TextView) findViewById(R.id.exStatus);
    exSetCb.setChecked(true);
    exFavCb.setChecked(true);
    exTrackCb.setChecked(true);
    fillSpinner();
    fillTypeSpinner();
    fillMapList();
    exOk.setOnClickListener(this);
    rcOk.setOnClickListener(this);
    UUID uuid = UUID.fromString("155155a2-e241-454a-a689-c6559116f28a");
    btUtil = new BluetoothUtil(uuid, NFC_SERVICE, this);
  }
  
  /** Import-Filebutton or Export-Button pressed. */
  @Override
  public void onClick(View v)
  {
    log("Selected: " + getSelectedType());
    if (getSelectedType() == EType.Import)
    { // Import a pmz file.
      String dataDir = ((PathElement)exSpinner.getSelectedItem()).getPath();
      String dataFile = ((Button)v).getText().toString();
      log("Import from: " + dataDir + "/" + dataFile);
      new MapUnzip().unzipImport(new File(dataDir, dataFile).getPath(), this.getApplicationContext());
      finish();
    }
    else if (getSelectedType() == EType.Transmit)
    {
      String dataDir = ((PathElement)exSpinner.getSelectedItem()).getPath();
      String dataFile = ((Button)v).getText().toString();
      log("Transmit from: " + dataDir + "/" + dataFile);
      if (!btUtil.isSupported())
      {
        logUser("Bluetooth is not supported");
      }
      else if (!BluetoothUtil.isPermissionAllowed(this))
      {
        logUser("Bluetooth: No permission");
        exStatus.setText("No permission, try again");
        exStatus.setTextColor(0xFFFF0000); // 0xAARRGGBB
        BluetoothUtil.requestPermission(this);
      }
      else if (!btUtil.isEnabled())
      {
        logUser("Bluetooth: Not enabled");
        exStatus.setText("Not enabled, try again");
        exStatus.setTextColor(0xFFFF0000); // 0xAARRGGBB
        btUtil.requestEnable(this);
      }
      else if (!btUtil.isConnected())
      {
        logUser("Bluetooth: Not connected");
        Handler h = new Handler()
        {
          @Override public void handleMessage(Message msg)
          {
            if (msg.what == BluetoothUtil.MSG_FAILED)
            {
              exStatus.setText("Connection failed, try again");
              exStatus.setTextColor(0xFFFF0000); // 0xAARRGGBB
            }
            else if (msg.what == BluetoothUtil.MSG_STARTED || msg.what == BluetoothUtil.MSG_PROGRESS)
            {
              exStatus.setText("Connecting in progress");
              exStatus.setTextColor(0xFF0000FF); // 0xAARRGGBB
            }
            else if (msg.what == BluetoothUtil.MSG_FINISH)
            {
              exStatus.setText("Connected, select again file to send");
              exStatus.setTextColor(0xFF0000FF); // 0xAARRGGBB
            }
          }
        };
        btUtil.connect(this, h);
      }
      else
      {
        logUser("Bluetooth: transmitting...");
        exStatus.setText("Transmitting in progress");
        exStatus.setTextColor(0xFF0000FF); // 0xAARRGGBB
        Handler han = new Handler()
        {
          @Override public void handleMessage(Message msg)
          {
            if (msg.what == BluetoothUtil.MSG_FAILED)
            {
              exStatus.setText("Transmission failed, try again");
              exStatus.setTextColor(0xFFFF0000); // 0xAARRGGBB
            }
            else if (msg.what == BluetoothUtil.MSG_STARTED)
            {
              exStatus.setText("Transmitting started");
              exStatus.setTextColor(0xFF0000FF); // 0xAARRGGBB
            }
            else if (msg.what == BluetoothUtil.MSG_FINISH)
            {
              exStatus.setText("Transmitting successful!");
              exStatus.setTextColor(0xFF00FF00); // 0xAARRGGBB
            }
            else if (msg.what == BluetoothUtil.MSG_PROGRESS)
            {
              int area = msg.arg1/10; // 0-10
              String bar = "[";
              for (int i=0; i<10; i++)
              {
                if (i<area) { bar += "X"; }
                else { bar += "  "; }
              }
              bar += "]";
              exStatus.setText(bar);
              exStatus.setTextColor(0xFF0000FF); // 0xAARRGGBB
            }
            else
            {
              log("Unknown message type: " + msg.what);
            }
          }
        };
        String dataPath = dataDir + "/" + dataFile;
        btUtil.transmit(new File(dataDir, dataFile), han);
      }
      log("End list-Paired-devices.");
    }
    else if (getSelectedType() == EType.Receive)
    {
      if (!btUtil.isSupported())
      {
        logUser("Bluetooth is not supported");
      }
      else if (!BluetoothUtil.isPermissionAllowed(this))
      {
        logUser("Bluetooth: No permission");
        exStatus.setText("No permission, try again");
        exStatus.setTextColor(0xFFFF0000); // 0xAARRGGBB
        BluetoothUtil.requestPermission(this);
      }
      else if (!btUtil.isEnabled())
      {
        logUser("Bluetooth: Not enabled");
        exStatus.setText("Not enabled, try again");
        exStatus.setTextColor(0xFFFF0000); // 0xAARRGGBB
        btUtil.requestEnable(this);
      }
      else
      {
        exStatus.setText("Receiving start ...");
        exStatus.setTextColor(0xFF0000FF); // 0xAARRGGBB
        String targetDir = ((PathElement)exSpinner.getSelectedItem()).getPath();
        receiveNow(targetDir);
      }
    }
    else
    {
      exportMapNow();
    }
  }
  
  private void receiveNow(String targetDir)
  {
    System.out.println("Receiving-Thread pre()");
    btUtil.startReceiver(BluetoothUtil.createFileReceiver(new File(targetDir), exStatus));
  }
  
  private File createNewFile(String targetDir)
  {
    GregorianCalendar now = new GregorianCalendar();
    int y = now.get(GregorianCalendar.YEAR);
    String m = "" + (now.get(GregorianCalendar.MONTH)+1);
    String d = "" + now.get(GregorianCalendar.DAY_OF_MONTH);
    if (m.length()==1) { m = "0" + m; }
    if (d.length()==1) { d = "0" + d; }
    return new File(targetDir, "" + y + "-" + m + "-" + d + "_PM.pmz");
  }
  
  private void exportMapNow()
  {
    String targetDir = ((PathElement)exSpinner.getSelectedItem()).getPath();
    if (!new File(targetDir).canWrite())
    {
      logUser("Error, can not write!");
      finish();
      return;
    }
    ArrayList<String> saveList = new ArrayList<String>();
    ArrayList<String> saveListDirs = new ArrayList<String>();
    String exSettingsS = "Exported: ";
    if (exSetCb.isChecked())
    {
      log("- Export settings.");
      boolean anySetting = false;
      for (String savingFile : Variable.getVariable().getSavingFiles())
      {
        saveList.add(savingFile);
        saveListDirs.add("");
        anySetting = true;
      }
      if (anySetting) { exSettingsS += "[Settings] "; }
    }
    if (exFavCb.isChecked())
    {
      log("- Export favourites.");
      String favFolder = Variable.getVariable().getMapsFolder().getParent();
      File favFile = new File(favFolder, "Favourites.properties");
      if (favFile.isFile())
      {
        saveList.add(favFile.getPath());
        saveListDirs.add("data");
        exSettingsS += "[Favourites] ";
      }
    }
    if (exTrackCb.isChecked())
    {
      log("- Export tracking-records.");
      File trDir = Variable.getVariable().getTrackingFolder();
      boolean anySetting = false;
      if (trDir.isDirectory())
      {
        for (String fname : trDir.list())
        {
          fname = new File(trDir, fname).getPath();
          saveList.add(fname);
          saveListDirs.add("data");
          anySetting = true;
        }
      }
      if (anySetting) { exSettingsS += "[Tracking-recs] "; }
    }
    final File zipFile = createNewFile(targetDir);
    if (saveList.isEmpty()) { logUser("Nothing to save."); }
    else
    {
      new MapUnzip().compressFiles(saveList, saveListDirs, zipFile.getPath(), null, this);
      ProgressPublisher pp = new ProgressPublisher(this.getApplicationContext());
      pp.updateTextFinal(exSettingsS);
    }
    if (exMapsCb.isChecked())
    {
      log("- Export maps.");
      logUser("Exporting maps ...");
      Thread t = new Thread(() ->
      { // Because this may be a long running task, we dont use AsyncTask.
        exportMaps(zipFile);
      });
      t.start();
    }
    finish();
  }
  
  public static FileType getFileType(String file)
  {
    if (file.startsWith("/maps/")) { return FileType.Map; }
    if (Variable.isSavingFile(file)) { return FileType.Setting; }
    if (file.endsWith(".gpx")) { return FileType.Tracking; }
    if (file.endsWith("Favourites.properties")) { return FileType.Favourites; }
    return FileType.Unknown;
  }
  
  /** One of the spinners changed. */
  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int i, long l)
  {
    exStatus.setText("-----");
    exStatus.setTextColor(0xFF000000); // 0xAARRGGBB
    boolean reloadFiles = false;
    if (parent == exTypeSpinner)
    {
      if (getSelectedType() == EType.Export)
      { // Export
        lExport.setVisibility(View.VISIBLE);
        lFList.setVisibility(View.GONE);
        lReceive.setVisibility(View.GONE);
      }
      else if (getSelectedType() == EType.Import)
      { // Import
        lExport.setVisibility(View.GONE);
        lFList.setVisibility(View.VISIBLE);
        lReceive.setVisibility(View.GONE);
        reloadFiles = true;
      }
      else if (getSelectedType() == EType.Transmit)
      { // Transmit
        lExport.setVisibility(View.GONE);
        lFList.setVisibility(View.VISIBLE);
        lReceive.setVisibility(View.GONE);
        reloadFiles = true;
      }
      else // if (getSelectedType() == EType.Receive)
      { // Receive
        lExport.setVisibility(View.GONE);
        lFList.setVisibility(View.GONE);
        lReceive.setVisibility(View.VISIBLE);
      }
    }
    else // parent == exSpinner
    {
      String dataDir = ((PathElement)exSpinner.getSelectedItem()).getPath();
      String dataDirShort = ((PathElement)exSpinner.getSelectedItem()).toString();
      if (dataDirShort.endsWith(dataDir)) { exFullPathTv.setText(""); }
      else { exFullPathTv.setText(dataDir); }
      reloadFiles = (getSelectedType()==EType.Import) || (getSelectedType() == EType.Transmit);
    }
    if (reloadFiles)
    {
        lFList.removeAllViews();
        String dataDir = ((PathElement)exSpinner.getSelectedItem()).getPath();
        for (String f : new File(dataDir).list())
        {
          if (!f.endsWith(".pmz") && !f.endsWith(".pmz.zip")) { continue; }
          if (!new File(dataDir, f).isFile()) { continue; }
          Button button = new Button(this);
          button.setText(f);
          button.setOnClickListener(this);
          LinearLayout ll = new LinearLayout(this);
          ll.setOrientation(LinearLayout.HORIZONTAL);
          ll.addView(button);
          Button delBut = new Button(this);
          delBut.setText("DEL");
          delBut.setOnClickListener(createDelListener(f));
          ll.addView(delBut);
          lFList.addView(ll);
        }
    }
  }
  
  private OnClickListener createDelListener(String f)
  {
    return new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        String dataDir = ((PathElement)exSpinner.getSelectedItem()).getPath();
        new File(dataDir, f).delete();
        ((ViewGroup)v.getParent().getParent()).removeView((ViewGroup)v.getParent());
      }
    };
  }

  @Override
  public void onNothingSelected(AdapterView<?> av) {}
  
  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
  {
    if (isChecked) { lMaps.setVisibility(View.VISIBLE); }
    else { lMaps.setVisibility(View.GONE); }
  }
  
  private void exportMaps(File zipBaseFile)
  {
    long fullSize = 0;
    for (View dView : lMaps.getTouchables())
    {
      CheckBox dCb = (CheckBox)dView;
      if (!dCb.isChecked()) { continue; }
      String dName = dCb.getText().toString();
      int zipDotIdx = zipBaseFile.getPath().lastIndexOf(".");
      String zipFile = zipBaseFile.getPath().substring(0, zipDotIdx) + "-" + dName + ".pmz";
      ArrayList<String> mapFiles = new ArrayList<String>();
      ArrayList<String> mapSubDirs = new ArrayList<String>();
      File mDir = new File(Variable.getVariable().getMapsFolder(), dName);
      if (!mDir.isDirectory()) { continue; }
      fullSize += IO.dirSize(mDir);
      long freeSize = zipBaseFile.getParentFile().getFreeSpace() / (1000 * 1000);
      if (fullSize > freeSize) { logUser("Out of disk-memory"); return; }
      for (String mFile : mDir.list())
      { // No directory allowed
        mapFiles.add(new File(mDir, mFile).getPath());
        mapSubDirs.add("/maps/" + dName);
      }
      if (mapFiles.isEmpty()) { continue; }
      ProgressPublisher pp = new ProgressPublisher(this.getApplicationContext());
      new MapUnzip().compressFiles(mapFiles, mapSubDirs, zipFile, pp, this);
    }
  }

  private void log(String str)
  {
    Log.i(ExportActivity.class.getName(), str);
  }
    
  private void logUser(String str)
  {
    Log.i(ExportActivity.class.getName(), str);
    runOnUiThread(() -> Toast.makeText(ExportActivity.this, str, Toast.LENGTH_SHORT).show());
  }

  private void fillSpinner()
  {
    ArrayAdapter<PathElement> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
    int counter = 1;
    for (String aPath : IO.listSelectionPaths(this, false))
    {
      adapter.add(new PathElement(aPath, counter));
      counter++;
    }
    exSpinner.setAdapter(adapter);
    exSpinner.setOnItemSelectedListener(this);
  }
  private void fillTypeSpinner()
  {
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
    adapter.add(getResources().getString(R.string.exp));
    adapter.add(getResources().getString(R.string.imp));
    adapter.add(getResources().getString(R.string.transmit));
    adapter.add(getResources().getString(R.string.receive));
    exTypeSpinner.setAdapter(adapter);
    exTypeSpinner.setSelection(0);
    exTypeSpinner.setOnItemSelectedListener(this);
  }
  
  private void fillMapList()
  {
    for (String dName : Variable.getVariable().getMapsFolder().list())
    {
      CheckBox cb = new CheckBox(this);
      cb.setText(dName);
      cb.setChecked(true);
      lMaps.addView(cb);
    }
    exMapsCb.setOnCheckedChangeListener(this);
  }
  
  static class PathElement
  {
    int maxLen = 10;
    String path;
    String visPath;
    
    public PathElement(String path, int counter)
    {
      this.path = path;
      this.visPath = toVisPath(new File(path).getName(), counter);
    }
    
    @Override
    public String toString()
    {
      return visPath;
    }
    
    private String toVisPath(String fn, int counter)
    {
      String ctStr = "(" + counter + ") ";
      if (path.length() > (maxLen + 6 + fn.length()))
      {
        return ctStr + path.substring(0, maxLen) + "... : " + fn;
      }
      return ctStr + path;
    }
    
    public String getPath() { return path; }
  }
}

