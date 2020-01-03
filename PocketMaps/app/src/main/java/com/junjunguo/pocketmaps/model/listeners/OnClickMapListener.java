package com.junjunguo.pocketmaps.model.listeners;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 15, 2015.
 */
public interface OnClickMapListener {
    /**
     * Tell Activity what to do when map FAB is clicked
     *
     * @param pos The map-position on ArrayAdapter.
     */
    void onClickMap(int pos);
}
