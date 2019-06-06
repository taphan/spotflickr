package com.cs550.teama.spotflickr.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.activity.hotspot.UpdateHotspotListActivity;
import com.cs550.teama.spotflickr.model.HotspotList;

import java.util.List;

public class HotspotListAdapter extends RecyclerView.Adapter<HotspotListAdapter.HotspotListViewHolder> {

    private Context mCtx;
    private List<HotspotList> hotspotListList;

    public HotspotListAdapter(Context mCtx, List<HotspotList> hotspotList) {
        this.mCtx = mCtx;
        this.hotspotListList = hotspotList;
    }

    @NonNull
    @Override
    public HotspotListAdapter.HotspotListViewHolder onCreateViewHolder(@android.support.annotation.NonNull ViewGroup viewGroup, int i) {
        return new HotspotListViewHolder(
                LayoutInflater.from(mCtx).inflate(R.layout.layout_hotspot_list, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(@android.support.annotation.NonNull HotspotListAdapter.HotspotListViewHolder hotspotListViewHolder, int i) {
        HotspotList hotspotList = hotspotListList.get(i);

        hotspotListViewHolder.textViewName.setText(hotspotList.getName());
        hotspotListViewHolder.textViewDesc.setText(hotspotList.getDescription());
    }

    @Override
    public int getItemCount() {
        return hotspotListList.size();
    }

    class HotspotListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewName, textViewDesc;
        //Button buttonListManager;

        public HotspotListViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textview_name);
            textViewDesc = itemView.findViewById(R.id.textview_desc);
            //buttonListManager = itemView.findViewById(R.id.button_list_manager);

            itemView.setOnClickListener(this);
            //buttonListManager.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            HotspotList hotspotList = hotspotListList.get(getAdapterPosition());
            Intent intent = new Intent(mCtx, UpdateHotspotListActivity.class);
            intent.putExtra("hotspotList", hotspotList);
            mCtx.startActivity(intent);

        }
    }
}
