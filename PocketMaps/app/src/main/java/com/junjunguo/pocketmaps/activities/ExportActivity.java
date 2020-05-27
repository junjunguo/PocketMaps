package com.junjunguo.pocketmaps.activities;

import com.junjunguo.pocketmaps.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import com.junjunguo.pocketmaps.downloader.MapUnzip;
import com.junjunguo.pocketmaps.downloader.ProgressPublisher;
import com.junjunguo.pocketmaps.util.IO;
import com.junjunguo.pocketmaps.util.Variable;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class ExportActivity  extends AppCompatActivity implements OnClickListener
{
    Spinner exSpinner;
    CheckBox exSetCb;
    CheckBox exFavCb;
    CheckBox exTrackCb;
    CheckBox exMapsCb;
  
  @Override protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_export);
    Button exOk = (Button) findViewById(R.id.exOk);
    TextView exHeader = (TextView) findViewById(R.id.exHeader);
    exSpinner = (Spinner) findViewById(R.id.exSpinner);
    exSetCb = (CheckBox) findViewById(R.id.exSet_cb);
    exFavCb = (CheckBox) findViewById(R.id.exFav_cb);
    exTrackCb = (CheckBox) findViewById(R.id.exTrack_cb);
    exMapsCb = (CheckBox) findViewById(R.id.exMaps_cb);
    exSetCb.setChecked(true);
    exFavCb.setChecked(true);
    exTrackCb.setChecked(true);
    fillSpinner();
    exHeader.setText("Export");
    exOk.setOnClickListener(this);
  }
  
  @Override
  public void onClick(View v)
  {
    if (v.getId()!=R.id.exOk) { return; }
    log("Selected: Export");
    String targetDir = exSpinner.getSelectedItem().toString();
    if (!new File(targetDir).canWrite())
    {
      logUser("Error, can not write!");
      finish();
      return;
    }
    ArrayList<String> saveList = new ArrayList<String>();
    if (exSetCb.isChecked())
    {
      log("- Export settings.");
      saveList.addAll(Variable.getVariable().getSavingFiles());
    }
    if (exFavCb.isChecked())
    {
      log("- Export favourites.");
      String favFolder = Variable.getVariable().getMapsFolder().getParent();
      saveList.add(new File(favFolder, "Favourites.properties").getPath());
    }
    if (exTrackCb.isChecked())
    {
      log("- Export tracking-records.");
      File trDir = Variable.getVariable().getTrackingFolder();
      if (trDir.isDirectory())
      {
        for (String fname : trDir.list())
        {
          fname = new File(trDir, fname).getPath();
          saveList.add(fname);
        }
      }
    }
    GregorianCalendar now = new GregorianCalendar();
    int y = now.get(GregorianCalendar.YEAR);
    String m = "" + (now.get(GregorianCalendar.MONTH)+1);
    String d = "" + now.get(GregorianCalendar.DAY_OF_MONTH);
    if (m.length()==1) { m = "0" + m; }
    if (d.length()==1) { d = "0" + d; }
    final File zipFile = new File(targetDir, "" + y + "-" + m + "-" + d + "_PM.pmz");
    if (saveList.isEmpty()) { logUser("Nothing to save."); }
    else
    {
      new MapUnzip().compressFiles(saveList, zipFile.getPath(), "data", null, this);
      logUser("Finish: Export base.");
    }
    if (exMapsCb.isChecked())
    {
      log("- Export maps.");
      logUser("Exporting maps ...");
      Thread t = new Thread(new Runnable(){ public void run()
      { // Because this may be a long running task, we dont use AsyncTask.
        exportMaps(zipFile);
      }});
      t.start();
    }
    finish();
  }
  
  private void exportMaps(File zipBaseFile)
  {
    for (String dName : Variable.getVariable().getMapsFolder().list())
    {
      int zipDotIdx = zipBaseFile.getPath().lastIndexOf(".");
      String zipFile = zipBaseFile.getPath().substring(0, zipDotIdx) + "-" + dName + ".pmz";
      ArrayList<String> mapFiles = new ArrayList<String>();
      File mDir = new File(Variable.getVariable().getMapsFolder(), dName);
      if (!mDir.isDirectory()) { continue; }
      for (String mFile : mDir.list())
      {
        mapFiles.add(new File(mDir, mFile).getPath());
      }
      if (mapFiles.size()==0) { continue; }
      ProgressPublisher pp = new ProgressPublisher(this.getApplicationContext());
      new MapUnzip().compressFiles(mapFiles, zipFile, "/maps/" + dName, pp, this);
    }
  }

  private void log(String str)
  {
    Log.i(ExportActivity.class.getName(), str);
  }
    
  private void logUser(String str)
  {
    Log.i(ExportActivity.class.getName(), str);
    try
    {
      Toast.makeText(this.getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }
    catch (Exception e) { e.printStackTrace(); }
  }

  private void fillSpinner()
  {
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
    adapter.addAll(IO.listSelectionPaths(this, false));
    exSpinner.setAdapter(adapter);
  }
}

