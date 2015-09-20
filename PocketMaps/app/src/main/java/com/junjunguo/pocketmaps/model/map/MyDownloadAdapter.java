package com.junjunguo.pocketmaps.model.map;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.dataType.MyMap;
import com.junjunguo.pocketmaps.model.listeners.MapFABonClickListener;
import com.junjunguo.pocketmaps.model.util.Constant;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class MyDownloadAdapter extends RecyclerView.Adapter<MyDownloadAdapter.ViewHolder> {
    private List<MyMap> myMaps;
    private MapFABonClickListener mapFABonClick;

    //    private int downloadingPosition;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public FloatingActionButton flag;
        public TextView name, continent, size, downloadStatus;
        public ProgressBar progressBar;
        public MapFABonClickListener mapFABonClick;


        public ViewHolder(View itemView, MapFABonClickListener mapFABonClick) {
            super(itemView);
            this.mapFABonClick = mapFABonClick;
            this.flag = (FloatingActionButton) itemView.findViewById(R.id.my_download_item_flag);
            this.name = (TextView) itemView.findViewById(R.id.my_download_item_name);
            this.continent = (TextView) itemView.findViewById(R.id.my_download_item_continent);
            this.size = (TextView) itemView.findViewById(R.id.my_download_item_size);
            this.downloadStatus = (TextView) itemView.findViewById(R.id.my_download_item_download_status);
            this.progressBar = (ProgressBar) itemView.findViewById(R.id.my_download_item_progress_bar);
        }

        public void setItemData(MyMap myMap) {
            int status = myMap.getStatus();

            switch (status) {
                case Constant.DOWNLOADING: {
                    flag.setImageResource(R.drawable.ic_pause_orange_24dp);
                    downloadStatus.setText("Downloading ..." +
                            String.format("%1$" + 3 + "s", Variable.getVariable().getMapFinishedPercentage()) + "%");
                    progressBar.setVisibility(View.VISIBLE);
                    //                    progressBar.setProgress(Variable.getVariable().getMapFinishedPercentage());
                    OnDownloading.getOnDownloading().setDownloadingProgressBar(downloadStatus, progressBar);
                    break;
                }
                case Constant.COMPLETE: {
                    flag.setImageResource(R.drawable.ic_map_white_24dp);
                    downloadStatus.setText("Downloaded");
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                }
                case Constant.PAUSE: {
                    flag.setImageResource(R.drawable.ic_play_arrow_light_green_a700_24dp);
                    downloadStatus.setText("Paused ..." +
                            String.format("%1$" + 3 + "s", Variable.getVariable().getMapFinishedPercentage()) + "%");
                    progressBar.setVisibility(View.VISIBLE);
                    //                    progressBar.setProgress(Variable.getVariable().getMapFinishedPercentage());
                    OnDownloading.getOnDownloading().setDownloadingProgressBar(downloadStatus, progressBar);
                    break;
                }

                default: {
                    flag.setImageResource(R.drawable.ic_cloud_download_white_24dp);
                    downloadStatus.setText("");
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                }
            }
            name.setText(myMap.getCountry());
            continent.setText(myMap.getContinent());
            size.setText(myMap.getSize());
            flag.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mapFABonClick.mapFABonClick(itemView);
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyDownloadAdapter(List<MyMap> myMaps, MapFABonClickListener mapFABonClick) {
        this.myMaps = myMaps;
        this.mapFABonClick = mapFABonClick;
        //        downloadingPosition = 999;
    }

    // Create new views (invoked by the layout manager)
    @Override public MyDownloadAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_download_item, parent, false);
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
