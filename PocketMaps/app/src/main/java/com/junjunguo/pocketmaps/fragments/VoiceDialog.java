package com.junjunguo.pocketmaps.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.navigator.NaviEngine;
import com.junjunguo.pocketmaps.navigator.NaviVoice;
import com.junjunguo.pocketmaps.util.MyUtility;
import com.junjunguo.pocketmaps.util.Variable;
import java.util.ArrayList;

/**
 *
 * @author ppp
 */
public class VoiceDialog
{
  private static final String engineList[] = {"AndroidTTS/null", "FLite/edu.cmu.cs.speech.tts.flite", "ESpeak/com.googlecode.eyesfree.espeak", "ESpeak-NG/com.reecedunn.espeak"};
    
  public static void showTtsVoiceSelector(final Activity activity)
  {
    if (NaviEngine.getNaviEngine().naviVoiceIsTtsReady(activity.getApplicationContext()))
    {
        showTtsVoiceSelectorNow(activity);
        return;
    }
    else if (NaviEngine.getNaviEngine().getNaviVoiceError() != null)
    {
        Toast.makeText(activity, NaviEngine.getNaviEngine().getNaviVoiceError(), Toast.LENGTH_SHORT);
    }
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle("TTS voice");
    builder1.setCancelable(true);
    TextView tview = new TextView(activity);
    tview.setText(R.string.engine_startup);
    
    builder1.setView(tview);
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
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
    ScrollView scrollView = new ScrollView(activity.getBaseContext());
    scrollView.addView(rg);
    builder1.setView(scrollView);
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
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
    DialogInterface.OnClickListener listenerSys = new DialogInterface.OnClickListener()
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
    final ArrayList<String> fullEngineList = new ArrayList<String>();
    java.util.Collections.addAll(fullEngineList, engineList);
    for (String curEngine : NaviEngine.getNaviEngine().naviVoiceEngineList(activity))
    {
        String curEnginePackage = curEngine.substring(curEngine.indexOf("/"));
        boolean curEngineExists = false;
        for (String staticEngine : engineList)
        {
            String staticEnginePackage = staticEngine.substring(staticEngine.indexOf("/"));
            if (curEnginePackage.equals(staticEnginePackage)) { curEngineExists = true; }
        }
        if (!curEngineExists) { fullEngineList.add(curEngine); }
    }
    for (int i=0; i<fullEngineList.size(); i++)
    {
        String appName = fullEngineList.get(i).split("/")[0];
        String appUrl = fullEngineList.get(i).split("/")[1];
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
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
          int selIndex = rg.getCheckedRadioButtonId();
          String selEngine = fullEngineList.get(selIndex).split("/")[1];
          if (selEngine.equals("null")) { selEngine = null; }
          Variable.getVariable().setTtsEngine(selEngine);
          Variable.getVariable().saveVariables(Variable.VarType.Base);
          NaviEngine.getNaviEngine().naviVoiceInit(activity, true);
      }
    };
    DialogInterface.OnClickListener listenerSys = new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
          int selIndex = rg.getCheckedRadioButtonId();
          String selEngine = fullEngineList.get(selIndex).split("/")[1];
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
    
}
