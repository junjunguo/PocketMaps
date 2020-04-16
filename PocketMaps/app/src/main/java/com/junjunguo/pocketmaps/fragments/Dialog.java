package com.junjunguo.pocketmaps.fragments;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.util.Variable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.provider.Settings;
import android.speech.tts.Voice;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.junjunguo.pocketmaps.navigator.NaviEngine;
import com.junjunguo.pocketmaps.navigator.NaviVoice;
import com.junjunguo.pocketmaps.util.MyUtility;
import java.util.ArrayList;

public class Dialog
{
  private static final String engineList[] = {"AndroidTTS/null", "FLite/edu.cmu.cs.speech.tts.flite", "ESpeak/com.googlecode.eyesfree.espeak", "ESpeak-NG/com.reecedunn.espeak"};
  
  public static void showAutoSelectMapSelector(Activity activity)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle(R.string.autoselect_map);
    builder1.setCancelable(true);
    final CheckBox cb = new CheckBox(activity.getBaseContext());
    cb.setChecked(Variable.getVariable().getAutoSelectMap());
    cb.setText(R.string.autoselect_map_text);
    builder1.setView(cb);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        Variable.getVariable().setAutoSelectMap(cb.isChecked());
        Variable.getVariable().saveVariables(Variable.VarType.Base);
      }
    };
    builder1.setPositiveButton(R.string.ok, listener);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
  
  public static void showTtsVoiceSelector(final Activity activity)
  {
    if (NaviEngine.getNaviEngine().naviVoiceIsTtsReady(activity.getApplicationContext()))
    {
        showTtsVoiceSelectorNow(activity);
        return;
    }
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle("TTS voice");
    builder1.setCancelable(true);
    TextView tview = new TextView(activity);
    tview.setText("TTS-VoiceEngine is starting up ... ");
    
    builder1.setView(tview);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
          showTtsVoiceSelector(activity);
      }
    };
    builder1.setPositiveButton(R.string.ok, listener);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
  
  public static void showTtsVoiceSelectorNow(final Activity activity)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle("TTS voice");
    builder1.setCancelable(true);
    final RadioGroup rg = new RadioGroup(activity.getBaseContext());
    
    RadioButton rb1 = new RadioButton(activity);
    rb1.setId(0);
    rb1.setText(activity.getResources().getString(R.string.any_voice));
    rg.addView(rb1);
    rg.check(0);
    
    ArrayList<String> voiceList = NaviEngine.getNaviEngine().naviVoiceList(activity.getApplicationContext());
    if (voiceList != null)
    {
      for (int i=0; i<voiceList.size(); i++)
      {
        RadioButton rb = new RadioButton(activity);
        rb.setId(i+1);
        String v = voiceList.get(i);
        rb.setText(v);
        rg.addView(rb);
        if (v.equals(Variable.getVariable().getTtsWantedVoice())) { rg.check(i+1); }
      }
    }
    builder1.setView(rg);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
          int selIndex = rg.getCheckedRadioButtonId();
          String selVoice = null;
          if (selIndex != 0) { selVoice = ((RadioButton)rg.getChildAt(selIndex)).getText().toString(); }
          Variable.getVariable().setTtsWantedVoice(selVoice);
          Variable.getVariable().saveVariables(Variable.VarType.Base);
          NaviEngine.getNaviEngine().naviVoiceSpeak(activity, "pocket maps", "pocket maps", true);
      }
    };
    OnClickListener listenerSys = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
          showTtsEngineSelector(activity);
      }
    };
    builder1.setPositiveButton(R.string.ok, listener);
    builder1.setNeutralButton("voice-engine", listenerSys);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
  
  public static void showTtsEngineSelector(final Activity activity)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle("TTS engine");
    builder1.setCancelable(true);
    final RadioGroup rg = new RadioGroup(activity.getBaseContext());
    for (int i=0; i<engineList.length; i++)
    {
        String appName = engineList[i].split("/")[0];
        String appUrl = engineList[i].split("/")[1];
        RadioButton rb = new RadioButton(activity);
        rb.setId(i);
        rb.setText(appName);
        boolean isInstalled = true;
        if (appUrl.equals("null")) {}
        else if (MyUtility.isAppInstalled(appUrl, activity)) {}
        else { isInstalled = false; rb.setText(appName + " [not installed]"); }
        rb.setEnabled(isInstalled);
        rg.addView(rb);
        
        String ttsUrl = Variable.getVariable().getTtsEngine();
        if (ttsUrl == null) { ttsUrl = "null"; }
        if (ttsUrl.equals(appUrl))
        {
            rg.check(rb.getId());
        }
    }
    builder1.setView(rg);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
          int selIndex = rg.getCheckedRadioButtonId();
          String selEngine = engineList[selIndex].split("/")[1];
          if (selEngine.equals("null")) { selEngine = null; }
          Variable.getVariable().setTtsEngine(selEngine);
          Variable.getVariable().saveVariables(Variable.VarType.Base);
          NaviEngine.getNaviEngine().naviVoiceInit(activity, true);
      }
    };
    OnClickListener listenerSys = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
          int selIndex = rg.getCheckedRadioButtonId();
          String selEngine = engineList[selIndex].split("/")[1];
          if (selEngine.equals("null"))
          {
            NaviVoice.showTtsActivity(activity);
          }
          else
          {
            NaviVoice.showAppActivity(activity, selEngine);
          }
      }
    };
    builder1.setPositiveButton(R.string.ok, listener);
    builder1.setNeutralButton("voice-config", listenerSys);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
  
  public static void showGpsSelector(final Activity activity)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle(R.string.autoselect_map);
    builder1.setCancelable(true);
    builder1.setTitle(R.string.gps_is_off);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivity(intent);
      }
    };
    builder1.setPositiveButton(R.string.gps_settings, listener);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
  
  public static void showUnitTypeSelector(Activity activity)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle(R.string.units);
    builder1.setCancelable(true);
    
    final RadioButton rb1 = new RadioButton(activity.getBaseContext());
    rb1.setText(R.string.units_metric);

    final RadioButton rb2 = new RadioButton(activity.getBaseContext());
    rb2.setText(R.string.units_imperal);
    
    final RadioGroup rg = new RadioGroup(activity.getBaseContext());
    rg.addView(rb1);
    rg.addView(rb2);
    rg.check(Variable.getVariable().isImperalUnit() ? rb2.getId() : rb1.getId());
    
    builder1.setView(rg);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        Variable.getVariable().setImperalUnit(rb2.isChecked());
        Variable.getVariable().saveVariables(Variable.VarType.Base);
      }
    };
    builder1.setPositiveButton(R.string.ok, listener);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
}
