package com.junjunguo.pocketmaps.model.util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.junjunguo.pocketmaps.R;

import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class MyDownloadAdapter extends RecyclerView.Adapter<MyDownloadAdapter.ViewHolder> {
    private List<MyMap> myMaps;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView flag;
        public TextView name, continent, size, downloadStatus;
        public ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            this.flag = (ImageView) itemView.findViewById(R.id.my_download_item_flag);
            this.name = (TextView) itemView.findViewById(R.id.my_download_item_name);
            this.continent = (TextView) itemView.findViewById(R.id.my_download_item_continent);
            this.size = (TextView) itemView.findViewById(R.id.my_download_item_size);
            this.downloadStatus = (TextView) itemView.findViewById(R.id.my_download_item_download_status);
            this.progressBar = (ProgressBar) itemView.findViewById(R.id.my_download_item_progress_bar);
        }

        public void setItemData(MyMap myMap) {
            if (myMap.isDownloaded()) {
                flag.setImageResource(R.drawable.ic_map_black_24dp);
                downloadStatus.setText("Downloaded");
                progressBar.setVisibility(View.INVISIBLE);
            } else if (myMap.isDownloading()) {
                downloadStatus.setText("Downloading file ...");
                progressBar.setVisibility(View.VISIBLE);
            } else {
                flag.setImageResource(R.drawable.ic_cloud_download_black_24dp);
                downloadStatus.setText("");
                progressBar.setVisibility(View.INVISIBLE);
            }
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
    public MyDownloadAdapter(List myMaps) {
        this.myMaps = myMaps;
    }

    // Create new views (invoked by the layout manager)
    @Override public MyDownloadAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_download_item, parent, false);
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
        myMaps.add(0, myMap);
        notifyItemInserted(0);
    }

}
