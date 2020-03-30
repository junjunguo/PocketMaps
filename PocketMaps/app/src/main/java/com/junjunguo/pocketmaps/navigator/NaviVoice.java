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
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.speech.tts.TextToSpeech.OnInitListener;
import java.util.ArrayList;

public class NaviVoice
{
  public static final String DISPLAY_LANG = Locale.getDefault().getDisplayLanguage();
  TextToSpeech tts;
  Voice curVoice;
  Locale wantedLang = new Locale(DISPLAY_LANG);
  Locale fallbackLang = Locale.ENGLISH;
  String wantedName;
  boolean ttsReady;
  boolean ttsMute = false;
  
  public NaviVoice(Context context)
  {
    tts = new TextToSpeech(context, createInitListener());
    updateVoiceCompat();
  }

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
    if (wantedName != null)
    {
      for (Voice v : curVoices)
      {
        if (v.getName().equals(wantedName)) { return v; }
      }
    }
    return curVoices.iterator().next();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public Set<Voice> getVoiceListGreater21(Locale lang)
  {
    if (!ttsReady) { return null; }
    Set<Voice> allV = tts.getVoices();
    Set<Voice> selV = new HashSet<>();
    for (Voice curV : allV)
    {
        if (curV.getLocale().getLanguage().equals(lang.getLanguage())) { selV.add(curV); }
    }
    return selV;
  }
  
  /** Can be set before androids TTS is ready. **/
  public void setVoice(Locale wantedLang, String wantedName)
  {
    this.wantedLang = wantedLang;
    this.wantedName = wantedName;
  }
  
  private OnInitListener createInitListener()
  {
    OnInitListener initList = new OnInitListener()
    {
      @Override
      public void onInit(int result)
      {
        if (result==TextToSpeech.SUCCESS) { ttsReady = true; }
      }
    };
    return initList;
  }
}
