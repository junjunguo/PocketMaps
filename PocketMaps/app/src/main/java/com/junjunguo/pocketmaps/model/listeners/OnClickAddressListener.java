package com.junjunguo.pocketmaps.model.listeners;

import android.location.Address;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 15, 2015.
 */
public interface OnClickAddressListener {
    /**
     * tell Activity what to do when address is clicked
     *
     * @param view
     */
    void onClick(Address addr);
}
