package com.junjunguo.pocketmaps.model.util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.junjunguo.pocketmaps.R;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 25, 2015.
 */
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
    private ItemData[] mDataset;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is a view template
        public ImageView icon;
        public TextView description;
        public TextView text;

        //        public ViewHolder(View itemView, ImageView icon, TextView description, TextView text) {
        public ViewHolder(View itemView) {
            super(itemView);
            this.icon = (ImageView) itemView.findViewById(R.id.nav_settings_template_icon_iv);
            this.description = (TextView) itemView.findViewById(R.id.map_nav_settings_template_des_tv);
            this.text = (TextView) itemView.findViewById(R.id.map_nav_settings_template_text_tv);
        }

        public void setItemData(ItemData itemData) {
            if (itemData.getIconResId() != 0) {
                System.out.println("==== " + itemData.toString() + "\n====" + icon);
                icon.setImageResource(itemData.getIconResId());
            }
            if (itemData.getDescription() != null) description.setText(itemData.getText());
            if (itemData.getText() != null) text.setText(itemData.getDescription());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SettingsAdapter(ItemData[] myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override public SettingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_nav_settings_template, parent, false);

        //        ImageView iv = (ImageView) parent.findViewById(R.id.nav_settings_template_icon_iv);
        //        TextView tvd = (TextView) parent.findViewById(R.id.map_nav_settings_template_des_tv);
        //        TextView tvt = (TextView) parent.findViewById(R.id.map_nav_settings_template_text_tv);
        // set the view's size, margins, paddings and layout parameters
        //        ...
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //        holder.setItemData(mDataset[position]);
        holder.icon.setImageResource(mDataset[position].getIconResId());
        holder.description.setText(mDataset[position].getDescription());
        holder.text.setText(mDataset[position].getText());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override public int getItemCount() {
        return mDataset.length;
    }


}
