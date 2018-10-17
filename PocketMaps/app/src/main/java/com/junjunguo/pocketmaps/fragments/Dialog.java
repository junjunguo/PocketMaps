package com.junjunguo.pocketmaps.fragments;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.util.Variable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.CheckBox;

public class Dialog
{
  public static void showAutoSelectMapSelector(Activity activity)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle(R.string.autoselect_map);
    builder1.setCancelable(true);
    final CheckBox cb = new CheckBox(activity.getBaseContext());
    cb.setChecked(Variable.getVariable().getAutoSelectMap());
    builder1.setView(cb);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        Variable.getVariable().setAutoSelectMap(cb.isChecked());
        Variable.getVariable().saveVariables();
      }
    };
    builder1.setPositiveButton(R.string.ok, listener);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
}
