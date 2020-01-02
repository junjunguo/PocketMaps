package com.junjunguo.pocketmaps.fragments;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.model.listeners.OnClickMapListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 03, 2015.
 */
public class MyMapAdapter extends RecyclerView.Adapter<MyMapAdapter.ViewHolder> {
    private List<MyMap> myMaps;
    private List<MyMap> myMapsFiltered;
    private OnClickMapListener onClickMapListener;
    private boolean isDownloadingView;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public FloatingActionButton flag;
        public TextView name, continent, size, downloadStatus;
        public OnClickMapListener onClickMapListener;
        private boolean isDownloadingView;

        protected ViewHolder(View itemView, OnClickMapListener onClickMapListener, boolean isDownloadingView) {
            super(itemView);
            this.isDownloadingView = isDownloadingView;
            this.onClickMapListener = onClickMapListener;
            this.flag = (FloatingActionButton) itemView.findViewById(R.id.my_maps_item_flag);
            this.name = (TextView) itemView.findViewById(R.id.my_maps_item_name);
            this.continent = (TextView) itemView.findViewById(R.id.my_maps_item_continent);
            this.size = (TextView) itemView.findViewById(R.id.my_maps_item_size);
            this.downloadStatus = (TextView) itemView.findViewById(R.id.my_maps_item_download_status);
        }

        public void setItemData(MyMap myMap) {
            name.setTextColor(android.graphics.Color.BLACK);
            if (isDownloadingView)
            {
                MyMap.DlStatus status = myMap.getStatus();

                if (status == MyMap.DlStatus.Downloading)
                {
                    flag.setImageResource(R.drawable.ic_pause_orange_24dp);
                    downloadStatus.setText("Downloading ...");
                }
                else if (status == MyMap.DlStatus.Unzipping)
                {
                  flag.setImageResource(R.drawable.ic_pause_orange_24dp);
                  downloadStatus.setText("Unzipping ...");
                }
                else if (status == MyMap.DlStatus.Complete)
                {
                  if (myMap.isUpdateAvailable())
                  {
                     flag.setImageResource(R.drawable.ic_cloud_download_white_24dp);
                     name.setTextColor(android.graphics.Color.RED);
                  }
                  else
                  {
                    flag.setImageResource(R.drawable.ic_map_white_24dp);
                  }
                  downloadStatus.setText("Downloaded");
                }
                else
                {
                  flag.setImageResource(R.drawable.ic_cloud_download_white_24dp);
                  downloadStatus.setText("");
                }
            }
            else
            {
                downloadStatus.setText("");
            }

            View.OnClickListener l = new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    log("onclick" + itemView.toString());
                    onClickMapListener.onClickMap(itemView, ViewHolder.this.getAdapterPosition(), downloadStatus);
                }
            };
            name.setText(myMap.getCountry());
            continent.setText(myMap.getContinent());
            size.setText(myMap.getSize());
            itemView.setOnClickListener(l);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyMapAdapter(List<MyMap> myMaps, OnClickMapListener onClickMapListener, boolean isDownloadingView)
    {
      this.myMaps = myMaps;
      this.myMapsFiltered = myMaps;
      this.onClickMapListener = onClickMapListener;
      this.isDownloadingView = isDownloadingView;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyMapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_maps_item, parent, false);
        ViewHolder vh = new ViewHolder(v, onClickMapListener, isDownloadingView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setItemData(myMapsFiltered.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myMapsFiltered.size();
    }
    
    public void refreshMapView(MyMap myMap)
    {
      int rvIndex = myMapsFiltered.indexOf(myMap);
      if (rvIndex >= 0)
      {
        notifyItemRemoved(rvIndex);
        notifyItemInserted(rvIndex);
      }
      else
      {
        log("No map-entry for refreshing found, maybe filter active.");
      }
    }

    /**
     * @param position
     * @return MyMap item at the position
     */
    public MyMap getItem(int position) {
        return myMapsFiltered.get(position);
    }

    /**
     * remove item at the given position
     *
     * @param position index
     */
    public MyMap remove(int position) {
        MyMap mm = null;
        if (myMaps == myMapsFiltered)
        {
          if (position >= 0 && position < getItemCount())
          {
            mm = myMaps.remove(position);
            notifyItemRemoved(position);
          }
        }
        else
        {
          log("WARNING: Cannot delete map on filtered mode.");
        }
        return mm;
    }

    /**
     * Clear the list (remove all elements)
     * Does NOT call notifyItemRangeRemoved()
     */
    public void clearList() {
        this.myMaps.clear();
        this.myMapsFiltered.clear();
    }

    /**
     * add a list of MyMap
     *
     * @param maps
     */
    public void addAll(List<MyMap> maps) {
        this.myMaps.addAll(maps);
        if (myMaps == myMapsFiltered)
        {
          notifyItemRangeInserted(myMaps.size() - maps.size(), maps.size());
        }
    }

    /**
     * Insert the object to the end of the list
     * Executes notifyItemInserted()
     * @param myMap
     */
    public void insert(MyMap myMap) {
        if (!getMapNameList().contains(myMap.getMapName())) {
            myMaps.add(myMap);
            if (myMaps == myMapsFiltered)
            {
              notifyItemInserted(getItemCount() - 1);
            }
        }
    }


    /**
     * @return a string list of map names (continent_country)
     */
    public List<String> getMapNameList() {
        ArrayList<String> al = new ArrayList<String>();
        for (MyMap mm : myMaps) {
            al.add(mm.getMapName());
        }
        return al;
    }
    
    static void log(String txt)
    {
      Log.i(MyMapAdapter.class.getName(), txt);
    }

    public void doFilter(String filterText)
    {
      log("FILTER-START!");
      filterText = filterText.toLowerCase();
      List<MyMap> filteredList = new ArrayList<MyMap>();
      if (filterText.isEmpty())
      {
        filteredList = myMaps;
        log("FILTER: Empty");
      }
      else
      {
        for (MyMap curMap : myMaps)
        {
          if (curMap.getCountry().toLowerCase().contains(filterText) || curMap.getContinent().toLowerCase().contains(filterText))
          {
            filteredList.add(curMap);
          }
        }
        log("FILTER: " + filteredList.size() + "/" + myMaps.size());
      }
      myMapsFiltered = filteredList;
      notifyDataSetChanged();
      log("FILTER: Publish: " + myMapsFiltered.size() + "/" + myMaps.size());
    }
}
