package com.junjunguo.pocketmaps.model.util;

import android.app.Activity;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * This file is part of Pocket Maps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on June 09, 2015.
 * <p>
 * <p>
 * Change status bar color
 */
public class SetStatusBarColor {

    public SetStatusBarColor() {
    }

    /**
     * set (statusBar: systemBar + actionBar) View to (color) with given (activity)
     *
     * @param statusBar View
     * @param color     int
     * @param activity  FragmentActivity
     */
    public void setStatusBarColor(View statusBar, int color, Activity activity) {
        //        System.out.println("------------------" + statusBar + "--" + color + "--" + activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = activity.getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //status bar height
            int actionBarHeight = getActionBarHeight(activity);
            int statusBarHeight = getStatusBarHeight(activity);
            //action bar height
            statusBar.getLayoutParams().height = actionBarHeight + statusBarHeight;
            statusBar.setBackgroundColor(color);
        }
    }
    /**
     * set (systemBar only) View to (color) with given (activity)
     *
     * @param statusBar View
     * @param color     int
     * @param activity  FragmentActivity
     */
    public void setSystemBarColor(View statusBar, int color, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = activity.getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            statusBar.getLayoutParams().height = getStatusBarHeight(activity);
            statusBar.setBackgroundColor(color);
        }
    }

    /**
     * @param activity
     * @return action bar height
     */
    public int getActionBarHeight(Activity activity) {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight =
                    TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    /**
     * @param activity
     * @return status bar height
     */
    public int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
