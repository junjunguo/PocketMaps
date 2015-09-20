package com.junjunguo.pocketmaps.model.map;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.dataType.MyMap;
import com.junjunguo.pocketmaps.model.listeners.MapFABonClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 03, 2015.
 */
public class MyMapAdapter extends RecyclerView.Adapter<MyMapAdapter.ViewHolder> {
    private List<MyMap> myMaps;
    private MapFABonClickListener mapFABonClick;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public MapFABonClickListener mapFABonClick;
        public FloatingActionButton flag;
        public TextView name, continent, size;

        public ViewHolder(View itemView, MapFABonClickListener mapFABonClick) {
            super(itemView);
            this.mapFABonClick = mapFABonClick;
            this.flag = (FloatingActionButton) itemView.findViewById(R.id.my_maps_item_flag);
            this.name = (TextView) itemView.findViewById(R.id.my_maps_item_name);
            this.continent = (TextView) itemView.findViewById(R.id.my_maps_item_continent);
            this.size = (TextView) itemView.findViewById(R.id.my_maps_item_size);
        }

        public void setItemData(MyMap myMap) {
            flag.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    log("onclick" + itemView.toString());
                    mapFABonClick.mapFABonClick(itemView);
                }
            });
            name.setText(myMap.getCountry());
            continent.setText(myMap.getContinent());
            size.setText(myMap.getSize());
        }

        public void log(String s) {System.out.println("++++" + this.getClass().getSimpleName() + "++++" + s);}

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyMapAdapter(List myMaps, MapFABonClickListener mapFABonClick) {
        this.myMaps = myMaps;
        this.mapFABonClick = mapFABonClick;
    }

    // Create new views (invoked by the layout manager)
    @Override public MyMapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_maps_item, parent, false);
        ViewHolder vh = new ViewHolder(v, mapFABonClick);
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
    public MyMap remove(int position) {
        MyMap mm = null;
        if (position >= 0 && position < getItemCount()) {
            mm = myMaps.remove(position);
            notifyItemRemoved(position);
        }
        return mm;
    }

    /**
     * remove all items
     */
    public void removeAll() {
        int i = myMaps.size();
        myMaps.retainAll(myMaps);
        notifyItemRangeRemoved(0, i);
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
//        log("insert called: " + myMap.toString());
        if (!getMapNameList().contains(myMap.getMapName())) {
//            log("insert: " + myMap.toString());
            myMaps.add(myMap);
            notifyItemInserted(getItemCount() - 1);
        }
    }


    /**
     * @return a string list of map names (continent_country)
     */
    public List getMapNameList() {
        ArrayList<String> al = new ArrayList();
        for (MyMap mm : myMaps) {
            al.add(mm.getMapName());
        }
        return al;
    }

    private void log(String s) {
        System.out.println("-------" + this.getClass().getSimpleName() + ": " + s);
    }
}
