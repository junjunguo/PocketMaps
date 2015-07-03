package com.junjunguo.pocketmaps.model.delete;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.junjunguo.pocketmaps.R;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 01, 2015.
 */
public class AppDownload {

    private static AppDownload appDownload;

    public static AppDownload getAppDownload() {
        if (appDownload == null) {
            appDownload = new AppDownload();
        }
        return appDownload;
    }

    private AppDownload() {
    }

    /**
     * init and set
     *
     * @param activity
     * @param calledFromVP
     */
    public void set(Activity activity, final ViewGroup calledFromVP) {
        ViewGroup appDownloadVP = (ViewGroup) activity.findViewById(R.id.app_settings_layout);
        clearBtn(activity, appDownloadVP, calledFromVP);

        appDownloadVP.setVisibility(View.VISIBLE);
        calledFromVP.setVisibility(View.INVISIBLE);
    }


    /**
     * init clear btn layout
     */
    private void clearBtn(Activity activity, final ViewGroup appdownloadVP, final ViewGroup calledFromVP) {
        //        ImageButton downloadClearBtn = (ImageButton) activity.findViewById(R.id.);
        //        downloadClearBtn.setOnClickListener(new View.OnClickListener() {
        //            @Override public void onClick(View v) {
        //                appdownloadVP.setVisibility(View.INVISIBLE);
        //                calledFromVP.setVisibility(View.VISIBLE);
        //            }
        //        });
    }


}
