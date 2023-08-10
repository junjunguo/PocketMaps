package com.junjunguo.pocketmaps.navigator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

import com.junjunguo.pocketmaps.util.Variable;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.Voice;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;
import com.junjunguo.pocketmaps.model.MyMap;
import java.util.ArrayList;

public class NaviVoice
{
  public static final String DISPLAY_LANG = Locale.getDefault().getISO3Language();
  TextToSpeech tts;
  Voice curVoice;
  Locale wantedLang = new Locale(DISPLAY_LANG);
  Locale fallbackLang = Locale.ENGLISH;
  boolean ttsReady;
  int ttsError = 0;
  boolean ttsMute = false;
  
  public NaviVoice(Context appContext)
  {
    String selEngine = Variable.getVariable().getTtsEngine();
    if (selEngine == null)
    { // Ensure to use applicationContext
        tts = new TextToSpeech(appContext.getApplicationContext(), createInitListener());
    }
    else
    { // Ensure to use applicationContext
        tts = new TextToSpeech(appContext.getApplicationContext(), createInitListener(), selEngine);
    }
    updateVoiceCompat();
  }
  
  public void shutdownTts() { tts.shutdown(); }

  public boolean isTtsReady()
  {
    return ttsReady;
  }
  
  public void setTtsMute(boolean ttsMute) { this.ttsMute = ttsMute; }
  
  public void speak(String fallbackTxt, String txt)
  {
    if (!ttsReady) { return; }
    if (!Variable.getVariable().isVoiceON()) { return; }
    if (ttsMute) { return; }
    updateVoiceCompat();
    if (wantedLang == fallbackLang) { speakCompat(fallbackTxt); }
    else { speakCompat(txt); }
  }
  
  /** Hint from https://stackoverflow.com/questions/27968146/texttospeech-with-api-21#29777304 **/
  private void speakCompat(String text)
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      ttsSpeakGreater21(text);
    }
    else
    {
      ttsSpeakUnder20(text);
    }
  }
  
  @SuppressWarnings("deprecation")
  private void ttsSpeakUnder20(String text)
  {
    HashMap<String, String> map = new HashMap<>();
    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
    tts.speak(text, TextToSpeech.QUEUE_ADD, map);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private void ttsSpeakGreater21(String text)
  {
    String utteranceId=this.hashCode() + "";
    tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
  }
  
  private void updateVoiceCompat()
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      updateVoiceGreater21();
    }
    else
    {
      updateVoiceUnder20();
    }
  }
  
  public ArrayList<String> getEngineList()
  {
    ArrayList<String> list = new ArrayList<String>();
    if (!ttsReady) { return list; }
    for (EngineInfo curEngine : tts.getEngines())
    {
        String packName = curEngine.name;
        int lastDot = packName.lastIndexOf(".");
        String engName = packName.substring(lastDot + 1);
        list.add(engName + "/" + packName);
    }
    return list;
  }
  
  public ArrayList<String> getVoiceListCompat()
  {
    if (!ttsReady) { return null; }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      HashSet<Voice> allVoices = new HashSet<Voice>();
      Set<Voice> curVoices = getVoiceListGreater21(wantedLang);
      log("Found " + curVoices.size() + " voices for " + wantedLang);
      if (curVoices != null) { allVoices.addAll(curVoices); }
      curVoices = getVoiceListGreater21(fallbackLang);
      log("Found " + curVoices.size() + " voices for " + fallbackLang);
      if (curVoices != null) { allVoices.addAll(curVoices); }
      ArrayList<String> curList = new ArrayList<String>();
      for (Voice v : allVoices)
      {
        curList.add(v.getLocale() + ":" + v.getName());
      }
      return curList;
    }
    else
    {
      return null;
    }
  }

  @SuppressWarnings("deprecation")
  private void updateVoiceUnder20()
  {
    Locale l = tts.getLanguage();
    if (l.equals(wantedLang) || l.equals(fallbackLang)) { return; }
    tts.setLanguage(wantedLang);
    if (!tts.getLanguage().equals(wantedLang))
    tts.setLanguage(fallbackLang);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private void updateVoiceGreater21()
  {
    if (!ttsReady) { return; }
    if (curVoice == null)
    {
      curVoice = searchWantedVoiceGreater21(wantedLang);
      if (curVoice == null)
      {
          wantedLang = fallbackLang;
          curVoice = searchWantedVoiceGreater21(fallbackLang);
      }
      if (curVoice == null) { return; }
    }
    else
    {
      if (tts.getVoice().equals(curVoice)) { return; }
    }
    tts.setVoice(curVoice);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private Voice searchWantedVoiceGreater21(Locale selLang)
  {
    Set<Voice> curVoices = getVoiceListGreater21(selLang);
    if (curVoices.size() == 0) { return null; }
    if (Variable.getVariable().getTtsWantedVoice() != null)
    {
      for (Voice v : curVoices)
      {
        if (v.getName().equals(Variable.getVariable().getTtsWantedVoice().split(":")[1]))
        {
            return v;
        }
      }
    }
    return curVoices.iterator().next();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private Set<Voice> getVoiceListGreater21(Locale lang)
  {
    if (!ttsReady) { return null; }
    Set<Voice> allV = tts.getVoices();
    if (allV==null) { return null; }
    Set<Voice> selV = new HashSet<>();
    for (Voice curV : allV)
    {
        if (curV.getLocale().getISO3Language().equals(lang.getISO3Language())) { selV.add(curV); }
    }
    return selV;
  }
  
  public static void showTtsActivity(Activity activity)
  {
    Intent installTTSIntent = new Intent();
    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
    ArrayList<String> languages = new ArrayList<String>();
    languages.add(DISPLAY_LANG);
    languages.add("en");
    installTTSIntent.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_CHECK_VOICE_DATA_FOR, languages);
    activity.startActivity(installTTSIntent);
  }
  
  public static void showAppActivity(Activity activity, String packageName)
  {
    try
    {
      PackageManager pm = activity.getPackageManager();
      Intent intent = pm.getLaunchIntentForPackage(packageName);
      activity.startActivity(intent);
    }
    catch (NullPointerException e)
    { //Some TTS-Apps are available, but cannot show. (pico)
      e.printStackTrace();
      logUserLong("Cannot show", activity);
    }
  }
  
  private OnInitListener createInitListener()
  {
    OnInitListener initList = new OnInitListener()
    {
      @Override
      public void onInit(int result)
      {
        if (result==TextToSpeech.SUCCESS) { ttsReady = true; }
        ttsError = result;
      }
    };
    return initList;
  }
  
  /** Returns error text, or null on no error. */
  public String getError()
  {
      if (ttsError==TextToSpeech.SUCCESS) { return null; }
      else if (ttsError==TextToSpeech.ERROR_NOT_INSTALLED_YET) { return "Error code: " + ttsError + "\nTTS not installed yet!"; }
      else if (ttsError==TextToSpeech.ERROR_SERVICE) { return "Error code: " + ttsError + "\nTTS Service!"; }
      else if (ttsError==TextToSpeech.ERROR_OUTPUT) { return "Error code: " + ttsError + "\nTTS Output!"; }
      else { return "Error code: " + ttsError; }
  }
  
  private void log(String txt)
  {
    Log.i(NaviVoice.class.getName(), txt);
  }
  
  private static void logUserLong(String str, Activity activity)
  {
    Log.i(MyMap.class.getName(), str);
    Toast.makeText(activity.getBaseContext(), str, Toast.LENGTH_LONG).show();
  }
}
