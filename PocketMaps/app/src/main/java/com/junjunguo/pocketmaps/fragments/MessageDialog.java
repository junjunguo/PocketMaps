package com.junjunguo.pocketmaps.fragments;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.junjunguo.pocketmaps.R;

public class MessageDialog
{
  public static void showMsg(Activity activity, String prefName, int msgId, boolean dontShowAgain)
  {
    SharedPreferences settings = activity.getSharedPreferences("DontShowAgain", 0);
    Boolean skipMessage = settings.getBoolean(prefName, false);
    if (skipMessage.equals(false) || !dontShowAgain)
    {
      Builder adb = createMsg(activity, prefName, msgId, dontShowAgain);
      adb.show();
    }
  }
  
  private static Builder createMsg(final Activity activity, final String prefName, int msgId, final boolean dontShowAgain)
  {
    final Builder adb = new Builder(activity);
    LayoutInflater adbInflater = LayoutInflater.from(activity);
    View view = adbInflater.inflate(R.layout.app_message, null);

    final CheckBox checkbox = (CheckBox) view.findViewById(R.id.msgCheckbox);
    if (!dontShowAgain) { checkbox.setVisibility(View.INVISIBLE); }
    adb.setView(view);
    adb.setTitle(R.string.settings);
    adb.setMessage(msgId);
    adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface dialog, int which)
      {
        if (dontShowAgain)
        {
          SharedPreferences settings = activity.getSharedPreferences("DontShowAgain", 0);
          SharedPreferences.Editor editor = settings.edit();
          editor.putBoolean(prefName, checkbox.isChecked());
          editor.commit();
        }
        dialog.cancel();
      }
    });
    return adb;
  }
}
