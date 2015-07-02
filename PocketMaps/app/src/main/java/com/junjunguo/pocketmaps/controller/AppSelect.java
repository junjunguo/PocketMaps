package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.junjunguo.pocketmaps.R;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 01, 2015.
 */
public class AppSelect {

    private static AppSelect appSelect;

    public static AppSelect getAppSelect() {
        if (appSelect == null) {
            appSelect = new AppSelect();
        }
        return appSelect;
    }

    private AppSelect() {
    }

    /**
     * init and set
     *
     * @param activity
     * @param calledFromVP
     */
    public void set(Activity activity, final ViewGroup calledFromVP) {
        ViewGroup appselectVP = (ViewGroup) activity.findViewById(R.id.app_settings_layout);
        clearBtn(activity, appselectVP, calledFromVP);

        appselectVP.setVisibility(View.VISIBLE);
        calledFromVP.setVisibility(View.INVISIBLE);
    }


    /**
     * init clear btn layout
     */
    private void clearBtn(Activity activity, final ViewGroup appselectVP, final ViewGroup calledFromVP) {
        //        ImageButton downloadClearBtn = (ImageButton) activity.findViewById(R.id.);
        //        downloadClearBtn.setOnClickListener(new View.OnClickListener() {
        //            @Override public void onClick(View v) {
        //                appdownloadVP.setVisibility(View.INVISIBLE);
        //                calledFromVP.setVisibility(View.VISIBLE);
        //            }
        //        });
    }
}
