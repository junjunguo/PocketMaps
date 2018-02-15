package com.junjunguo.pocketmaps.fragments;

import android.location.Address;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.listeners.OnClickAddressListener;

import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 03, 2015.
 */
public class MyAddressAdapter extends RecyclerView.Adapter<MyAddressAdapter.ViewHolder> {
    private List<Address> addressList;
    private OnClickAddressListener onClickAddressListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public OnClickAddressListener onClickAddressListener;
        public TextView firstLine, secondLine, thirdLine, fourthLine;

        public ViewHolder(View itemView, OnClickAddressListener onClickAddressListener) {
            super(itemView);
            this.onClickAddressListener = onClickAddressListener;
            this.firstLine = (TextView) itemView.findViewById(R.id.mapFirstLineTxt);
            this.secondLine = (TextView) itemView.findViewById(R.id.mapSecondLineTxt);
            this.thirdLine = (TextView) itemView.findViewById(R.id.mapThirdLineTxt);
            this.fourthLine = (TextView) itemView.findViewById(R.id.mapFourthLineTxt);
        }

        public void setItemData(final Address address) {
            View.OnClickListener clickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    log("onClick: " + itemView.toString());
                    onClickAddressListener.onClick(address);
                }
              
            };
            firstLine.setOnClickListener(clickListener);
            secondLine.setOnClickListener(clickListener);
            thirdLine.setOnClickListener(clickListener);
            fourthLine.setOnClickListener(clickListener);
            setText(firstLine, address.getAddressLine(0));
            setText(secondLine, address.getAddressLine(1));
            setText(thirdLine, address.getAddressLine(2));
            setText(fourthLine, address.getAddressLine(3));
        }

        private void setText(TextView curLine, String addressLine)
        {
          if (addressLine==null || addressLine.isEmpty())
          {
            curLine.setText(R.string.location);
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
    public MyAddressAdapter(List<Address> addressList, OnClickAddressListener onClickAddressListener) {
        this.addressList = addressList;
        this.onClickAddressListener = onClickAddressListener;
    }

    // Create new views (invoked by the layout manager)
    @Override public MyAddressAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_entry, parent, false);
        ViewHolder vh = new ViewHolder(v, onClickAddressListener);
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
