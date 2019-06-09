package com.cs550.teama.spotflickr.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.model.Hotspot;

import java.util.List;

public class HotspotAdapter extends RecyclerView.Adapter<HotspotAdapter.HotspotViewHolder>{

    private Context mCtx;
    private List<Hotspot> hotspotList;

    public HotspotAdapter(Context mCtx, List<Hotspot> hotspotList) {
        this.mCtx = mCtx;
        this.hotspotList = hotspotList;
    }

    @NonNull
    @Override
    public HotspotAdapter.HotspotViewHolder onCreateViewHolder(@android.support.annotation.NonNull ViewGroup viewGroup, int i) {
        return new HotspotAdapter.HotspotViewHolder(
                LayoutInflater.from(mCtx).inflate(R.layout.layout_hotspot, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(@android.support.annotation.NonNull HotspotAdapter.HotspotViewHolder hotspotViewHolder, int i) {
        Hotspot hotspot = hotspotList.get(i);

        hotspotViewHolder.textViewName.setText(hotspot.getName());
        hotspotViewHolder.textViewLatitude.setText(Float.toString(hotspot.getLatitude()));
        hotspotViewHolder.textViewLongitude.setText(Float.toString(hotspot.getLongitude()));
        hotspotViewHolder.textViewDesc.setText(hotspot.getDescription());
    }

    @Override
    public int getItemCount() {
        return hotspotList.size();
    }

    class HotspotViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewName, textViewLatitude, textViewLongitude, textViewDesc;
        Button buttonHotspotManager;

        public HotspotViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textview_name);
            textViewLatitude = itemView.findViewById(R.id.textview_latitude);
            textViewLongitude = itemView.findViewById(R.id.textview_longitude);
            textViewDesc = itemView.findViewById(R.id.textview_desc);
            buttonHotspotManager = itemView.findViewById(R.id.button_hotspot_manager);

            itemView.setOnClickListener(this);
            buttonHotspotManager.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Hotspot hotspot = hotspotList.get(getAdapterPosition());
            Intent intent;
            switch (v.getId()) {
                case R.id.button_list_manager:
                    //intent = new Intent(mCtx, UpdateHotspotActivity.class);
                    //intent.putExtra("hotspot", hotspot);
                    //mCtx.startActivity(intent);
                    break;
            }
        }
    }
}
