package com.junjunguo.pocketmaps.model.util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.Navigator;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 28, 2015.
 */
public class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.ViewHolder> {
    private InstructionList instructions;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView description, distance, time;

        public ViewHolder(View itemView) {
            super(itemView);
            this.icon = (ImageView) itemView.findViewById(R.id.nav_instruction_item_sign_iv);
            this.description = (TextView) itemView.findViewById(R.id.nav_instruction_item_description_tv);
            this.distance = (TextView) itemView.findViewById(R.id.nav_instruction_item_distance_tv);
//            this.time = (TextView) itemView.findViewById(R.id.nav_instruction_item_time_tv);
        }

        public void setItemData(Instruction itemData) {
                        icon.setImageResource(Navigator.getNavigator().getDirectionSign(itemData));
            description.setText(Navigator.getNavigator().getDirectionDescription(itemData));
            distance.setText(Navigator.getNavigator().getDistance(itemData));
//            time.setText(String.valueOf(Navigator.getNavigator().getTime(itemData)));
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public InstructionAdapter(InstructionList instructions) {
        this.instructions = instructions;
    }

    // Create new views (invoked by the layout manager)
    @Override public InstructionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_nav_instruction_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.setItemData(instructions.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override public int getItemCount() {
        return instructions.size();
    }


}
