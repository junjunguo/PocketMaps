package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.junjunguo.pocketmaps.R;

import java.util.List;

/**
 * TODO: user can save locations as their favorite in favorite list
 * <p/>
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 03, 2015.
 */
public class FavoriteList {

    /**
     * active favorite list
     */
    private void activeRecyclerView(List myMaps, Activity activity) {
        RecyclerView mapsRV;
        RecyclerView.Adapter adapter;
        RecyclerView.LayoutManager layoutManager;

        mapsRV = (RecyclerView) activity.findViewById(R.id.my_maps_recycler_view);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(1000);
        animator.setRemoveDuration(1000);
        mapsRV.setItemAnimator(animator);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mapsRV.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(activity);
        mapsRV.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        //        adapter = new MyMapAdapter(myMaps);
        //        mapsRV.setAdapter(adapter);

        // swipe left or right to remove an item
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        //Remove swiped item from list and notify the RecyclerView
                        //                        logToast("on swiped ...");
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(mapsRV);
    }

}
