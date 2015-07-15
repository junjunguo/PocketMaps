package com.junjunguo.pocketmaps.model.delete;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 03, 2015.
 */
public class RVItemTouchListener implements RecyclerView.OnItemTouchListener {

    private OnItemTouchListener mListener;

    public interface OnItemTouchListener {
        boolean onItemTouch(View view, int position, MotionEvent e);
    }


    public RVItemTouchListener(OnItemTouchListener listener) {
        mListener = listener;
    }

    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null) {
            return mListener.onItemTouch(childView, view.getChildAdapterPosition(childView), e);
        }
        return false;
    }

    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
