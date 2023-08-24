package com.junjunguo.pocketmaps.activities;

import com.junjunguo.pocketmaps.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.junjunguo.pocketmaps.downloader.MapUnzip;
import com.junjunguo.pocketmaps.downloader.ProgressPublisher;
import com.junjunguo.pocketmaps.util.IO;
import com.junjunguo.pocketmaps.util.Variable;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class ExportActivity  extends AppCompatActivity implements OnClickListener, OnItemSelectedListener, OnCheckedChangeListener
{
    public enum FileType { Tracking, Favourites, Setting, Map, Unknown }
    Spinner exSpinner;
    Spinner exTypeSpinner;
    CheckBox exSetCb;
    CheckBox exFavCb;
    CheckBox exTrackCb;
    CheckBox exMapsCb;
    TextView exFullPathTv;
    LinearLayout lImport;
    LinearLayout lExport;
    LinearLayout lMaps;
  
  @Override protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_export);
    Button exOk = (Button) findViewById(R.id.exOk);
    exSpinner = (Spinner) findViewById(R.id.exSpinner);
    exTypeSpinner = (Spinner) findViewById(R.id.exTypeSpinner);
    exFullPathTv = (TextView) findViewById(R.id.exFullPathTv);
    exSetCb = (CheckBox) findViewById(R.id.exSet_cb);
    exFavCb = (CheckBox) findViewById(R.id.exFav_cb);
    exTrackCb = (CheckBox) findViewById(R.id.exTrack_cb);
    exMapsCb = (CheckBox) findViewById(R.id.exMaps_cb);
    lImport = (LinearLayout) findViewById(R.id.exLayout_import);
    lExport = (LinearLayout) findViewById(R.id.exLayout_export);
    lMaps = (LinearLayout) findViewById(R.id.exLayout_maps);
    exSetCb.setChecked(true);
    exFavCb.setChecked(true);
    exTrackCb.setChecked(true);
    fillSpinner();
    fillTypeSpinner();
    fillMapList();
    exOk.setOnClickListener(this);
  }
  
  /** Import-Filebutton or Export-Button pressed. */
  @Override
  public void onClick(View v)
  {
    if (v.getId()!=R.id.exOk)
    { // Import a pmz file.
      String dataDir = ((PathElement)exSpinner.getSelectedItem()).getPath();
      String dataFile = ((Button)v).getText().toString();
      log("Import from: " + dataDir + "/" + dataFile);
      new MapUnzip().unzipImport(new File(dataDir, dataFile).getPath(), this.getApplicationContext());
      finish();
      return;
    }
    log("Selected: Export");
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
      new MapUnzip().compressFiles(saveList, saveListDirs, zipFile.getPath(), null, this);
      ProgressPublisher pp = new ProgressPublisher(this.getApplicationContext());
      pp.updateTextFinal(exSettingsS);
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
    if (parent == exTypeSpinner)
    {
      if (exTypeSpinner.getSelectedItemId()==0)
      {
        lExport.setVisibility(View.VISIBLE);
        lImport.setVisibility(View.GONE);
      }
      else
      {
        lExport.setVisibility(View.GONE);
        lImport.setVisibility(View.VISIBLE);
        lImport.removeAllViews();
        String dataDir = ((PathElement)exSpinner.getSelectedItem()).getPath();
        for (String f : new File(dataDir).list())
        {
          if (!f.endsWith(".pmz") && !f.endsWith(".pmz.zip")) { continue; }
          if (!new File(dataDir, f).isFile()) { continue; }
          Button button = new Button(this);
          button.setText(f);
          button.setOnClickListener(this);
          lImport.addView(button);
        }
      }
    }
    else // parent == exSpinner
    {
      String dataDir = ((PathElement)exSpinner.getSelectedItem()).getPath();
      String dataDirShort = ((PathElement)exSpinner.getSelectedItem()).toString();
      if (dataDirShort.endsWith(dataDir)) { exFullPathTv.setText(""); }
      else { exFullPathTv.setText(dataDir); }
    }
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
      if (mapFiles.size()==0) { continue; }
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
    try
    {
      Toast.makeText(this.getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }
    catch (Exception e) { e.printStackTrace(); }
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

