package com.junjunguo.pocketmaps.model.listeners;

import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 09, 2015.
 */
public interface OnDownloadingListener {
    void progressbarReady(TextView downloadStatus, ProgressBar progressBar);
}
