package com.junjunguo.pocketmaps.fragments;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.util.Variable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Dialog
{
  
  public static void showAutoSelectMapSelector(Activity activity)
  {
    final CheckBox cb = new CheckBox(activity.getBaseContext());
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        Variable.getVariable().setAutoSelectMap(cb.isChecked());
        Variable.getVariable().saveVariables(Variable.VarType.Base);
      }
    };
    showBooleanSelector(activity, cb, R.string.autoselect_map, R.string.autoselect_map_text, Variable.getVariable().getAutoSelectMap(), listener);
  }
  
  public static void showHintTextSelector(Activity activity)
  {
    final CheckBox cb = new CheckBox(activity.getBaseContext());
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        Variable.getVariable().setShowHintText(cb.isChecked());
        Variable.getVariable().saveVariables(Variable.VarType.Base);
      }
    };
    showBooleanSelector(activity, cb, R.string.show_hints, R.string.show_hints, Variable.getVariable().getShowHintText(), listener);
  }
  
  private static void showBooleanSelector(Activity activity, CheckBox cb, int titleId, int txtId, boolean curSetting, OnClickListener listener)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle(titleId);
    builder1.setCancelable(true);
    cb.setChecked(curSetting);
    cb.setText(txtId);
    builder1.setView(cb);
    builder1.setPositiveButton(R.string.ok, listener);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
  
  public static void showGpsSelector(final Activity activity)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
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
