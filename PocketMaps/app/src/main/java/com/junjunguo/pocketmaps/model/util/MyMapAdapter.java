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
 * <p>
 * Created by GuoJunjun <junjunguo.com> on July 03, 2015.
 */
public class MyMapAdapter extends RecyclerView.Adapter<MyMapAdapter.ViewHolder> {
    private List<MyMap> myMaps;

    public static class ViewHolder extends RecyclerView.ViewHolder {
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
}
