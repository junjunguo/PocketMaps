package com.junjunguo.pocketmaps.fragments;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.model.listeners.OnClickMapListener;
import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class MyDownloadAdapter extends RecyclerView.Adapter<MyDownloadAdapter.ViewHolder> {
    private List<MyMap> myMaps;
    private OnClickMapListener onClickMapListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public FloatingActionButton flag;
        public TextView name, continent, size, downloadStatus;
        public OnClickMapListener onClickMapListener;

        public ViewHolder(View itemView, OnClickMapListener onClickMapListener) {
            super(itemView);
            this.onClickMapListener = onClickMapListener;
            this.flag = (FloatingActionButton) itemView.findViewById(R.id.my_download_item_flag);
            this.name = (TextView) itemView.findViewById(R.id.my_download_item_name);
            this.continent = (TextView) itemView.findViewById(R.id.my_download_item_continent);
            this.size = (TextView) itemView.findViewById(R.id.my_download_item_size);
            this.downloadStatus = (TextView) itemView.findViewById(R.id.my_download_item_download_status);
        }

        public void setItemData(MyMap myMap) {
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
            name.setText(myMap.getCountry());
            continent.setText(myMap.getContinent());
            size.setText(myMap.getSize());
            flag.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                  onClickMapListener.onClickMap(itemView, ViewHolder.this.getAdapterPosition(), downloadStatus);
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyDownloadAdapter(List<MyMap> myMaps, OnClickMapListener onClickMapListener)
    {
      this.myMaps = myMaps;
      this.onClickMapListener = onClickMapListener;
    }

    // Create new views (invoked by the layout manager)
    @Override public MyDownloadAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_download_item, parent, false);
        ViewHolder vh = new ViewHolder(v, onClickMapListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setItemData(myMaps.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override public int getItemCount() {
        return myMaps.size();
    }

    /**
     * @param position index
     * @return MyMap item at the position
     */
    public MyMap getItem(int position) {
        return myMaps.get(position);
    }

    /**
     * get MyMap object position by its mapName variable
     *
     * @param mapName map name
     * @return -1 if not found;
     */
    public int getPosition(String mapName) {
        for (int i = 0; i < myMaps.size(); i++) {
            if (myMaps.get(i).getMapName().equalsIgnoreCase(mapName)) {

                return i;
            }
        }
        return -1;
    }

    /**
     * remove item at the given position
     *
     * @param position index
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
     * clear the list (remove all elements)
     */
    public void clearList() {
        this.myMaps.clear();
    }

    /**
     * add a list of MyMap
     *
     * @param maps list of MyMap
     */
    public void addAll(List<MyMap> maps) {
        this.myMaps.addAll(maps);
        notifyItemRangeInserted(myMaps.size() - maps.size(), maps.size());
    }

    /**
     * insert the object to the end of the list
     *
     * @param myMap MyMap
     */
    public void insert(MyMap myMap) {
        myMaps.add(0, myMap);
        notifyItemInserted(0);
    }

    /**
     * @return MyMaps list
     */
    public List<MyMap> getMaps() {
        return myMaps;
    }

}
