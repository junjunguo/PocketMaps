package com.junjunguo.pocketmaps.model.util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.junjunguo.pocketmaps.R;

import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 03, 2015.
 */
public class MyMapAdapter extends RecyclerView.Adapter<MyMapAdapter.ViewHolder> {
    private List<MyMap> myMaps;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //        public ImageView flag;
        public TextView name, continent, size;

        public ViewHolder(View itemView) {
            super(itemView);
            //            this.flag = (ImageView) itemView.findViewById(R.id.my_maps_item_flag);
            this.name = (TextView) itemView.findViewById(R.id.my_maps_item_name);
            this.continent = (TextView) itemView.findViewById(R.id.my_maps_item_continent);
            this.size = (TextView) itemView.findViewById(R.id.my_maps_item_size);
        }

        public void setItemData(MyMap myMap) {
            //            flag.setImageResource();
            name.setText(myMap.getName());
            continent.setText(myMap.getContinent());
            size.setText(myMap.getSize());
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override public void onClick(View v) {

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyMapAdapter(List myMaps) {
        this.myMaps = myMaps;
    }

    // Create new views (invoked by the layout manager)
    @Override public MyMapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_maps_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.setItemData(myMaps.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override public int getItemCount() {
        return myMaps.size();
    }

    /**
     * @param position
     * @return MyMap item at the position
     */
    public MyMap getItem(int position) {
        return myMaps.get(position);
    }

    /**
     * remove item at the given position
     *
     * @param position
     */
    public void remove(int position) {
        if (position >= 0 && position < getItemCount()) {
            myMaps.remove(position);
            notifyItemRemoved(position);
        }
    }


    /**
     * add a list of MyMap
     *
     * @param maps
     */
    public void addAll(List maps) {
        this.myMaps.addAll(maps);
        notifyItemRangeInserted(myMaps.size() - maps.size(), maps.size());
    }

    /**
     * insert the object to the end of the list
     *
     * @param myMap
     */
    public void insert(MyMap myMap) {
        myMaps.add(myMap);
        notifyItemInserted(getItemCount() - 1);
    }

}
