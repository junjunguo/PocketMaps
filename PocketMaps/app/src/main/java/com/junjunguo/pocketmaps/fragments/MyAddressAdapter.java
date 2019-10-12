package com.junjunguo.pocketmaps.fragments;

import android.location.Address;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.geocoding.AddressLoc;
import com.junjunguo.pocketmaps.model.listeners.OnClickAddressListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 03, 2015.
 */
public class MyAddressAdapter extends RecyclerView.Adapter<MyAddressAdapter.ViewHolder> {
    private List<Address> addressList;
    private OnClickAddressListener onClickAddressListener;
    private OnClickAddressListener onClickDetailsListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public OnClickAddressListener onClickAddressListener;
        public OnClickAddressListener onClickDetailsListener;
        public TextView firstLine, secondLine, thirdLine, fourthLine;
        ImageView addrDetailsButton;
        public ViewHolder(View itemView, OnClickAddressListener onClickAddressListener, OnClickAddressListener onClickDetailsListener) {
            super(itemView);
            this.onClickAddressListener = onClickAddressListener;
            this.onClickDetailsListener = onClickDetailsListener;
            this.firstLine = (TextView) itemView.findViewById(R.id.mapFirstLineTxt);
            this.secondLine = (TextView) itemView.findViewById(R.id.mapSecondLineTxt);
            this.thirdLine = (TextView) itemView.findViewById(R.id.mapThirdLineTxt);
            this.fourthLine = (TextView) itemView.findViewById(R.id.mapFourthLineTxt);
            this.addrDetailsButton = (ImageView) itemView.findViewById(R.id.iconAddressDetail);
        }

        public void setItemData(final Address address) {
            View.OnClickListener clickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    log("onClick: " + itemView.toString());
                    onClickAddressListener.onClick(address);
                }
            };
            View.OnClickListener clickDetListener = new View.OnClickListener() {
                public void onClick(View v) {
                    log("onClick: " + itemView.toString());
                    onClickDetailsListener.onClick(address);
                }
            };
            firstLine.setOnClickListener(clickListener);
            secondLine.setOnClickListener(clickListener);
            thirdLine.setOnClickListener(clickListener);
            fourthLine.setOnClickListener(clickListener);
            addrDetailsButton.setOnClickListener(clickDetListener);
            ArrayList<String> lines = AddressLoc.getLines(address);
            while (lines.size() < 4) { lines.add(""); }
            setText(firstLine, lines.get(0));
            setText(secondLine, lines.get(1).split("\n")[0]);
            setText(thirdLine, lines.get(2).split("\n")[0]);
            setText(fourthLine, lines.get(3).split("\n")[0]);
        }

        private void setText(TextView curLine, String addressLine)
        {
          if (addressLine==null || addressLine.isEmpty())
          {
            curLine.setText("");
          }
          else if (addressLine.length() > 35)
          {
            curLine.setText(addressLine.substring(0, 33) + "...");
          }
          else
          {
            curLine.setText(addressLine);
          }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAddressAdapter(List<Address> addressList, OnClickAddressListener onClickAddressListener, OnClickAddressListener onClickDetailsListener) {
        this.addressList = addressList;
        this.onClickAddressListener = onClickAddressListener;
        this.onClickDetailsListener = onClickDetailsListener;
    }

    // Create new views (invoked by the layout manager)
    @Override public MyAddressAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_entry, parent, false);
        ViewHolder vh = new ViewHolder(v, onClickAddressListener, onClickDetailsListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.setItemData(addressList.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override public int getItemCount() {
        return addressList.size();
    }

    /**
     * @param position
     * @return MyMap item at the position
     */
    public Address getItem(int position) {
        return addressList.get(position);
    }

    /**
     * remove item at the given position
     *
     * @param position
     * @return The removed item.
     */
    public Address remove(int position) {
        Address mm = null;
        if (position >= 0 && position < getItemCount()) {
            mm = addressList.remove(position);
            notifyItemRemoved(position);
        }
        return mm;
    }

    /**
     * remove all items
     */
    public void removeAll() {
        int i = addressList.size();
        addressList.clear();
        notifyItemRangeRemoved(0, i);
    }

    /**
     * add a list of Addresses
     *
     * @param maps
     */
    public void addAll(List<Address> addr) {
        this.addressList.addAll(addr);
        notifyItemRangeInserted(addressList.size() - addr.size(), addr.size());
    }

    /**
     * insert the object to the end of the list
     *
     * @param myMap
     */
    public void insert(Address address) {
            addressList.add(address);
            notifyItemInserted(getItemCount() - 1);
    }
    
    public static void log(String s)
    {
      Log.i(MyAddressAdapter.class.getName(), s);
    }
}
