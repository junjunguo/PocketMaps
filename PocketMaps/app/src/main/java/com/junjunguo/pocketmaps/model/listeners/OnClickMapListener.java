package com.junjunguo.pocketmaps.model.listeners;

import android.view.View;
import android.widget.TextView;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 15, 2015.
 */
public interface OnClickMapListener {
    /**
     * tell Activity what to do when map FAB is clicked
     *
     * @param view
     * @param downloadStatus 
     */
    void onClickMap(View view, int pos, TextView downloadStatus);
}
