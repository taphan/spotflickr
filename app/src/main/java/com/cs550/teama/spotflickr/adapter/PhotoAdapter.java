package com.cs550.teama.spotflickr.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.model.photo.Photo;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private ArrayList<Photo> dataList;

    public PhotoAdapter(ArrayList<Photo> dataList) {
        this.dataList = dataList;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.single_view_row, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        holder.txtPhotoTitle.setText(dataList.get(position).getTitle());
        holder.txtPhotoFilePath.setText(dataList.get(position).getId());
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {

        TextView txtPhotoTitle, txtPhotoFilePath;

        PhotoViewHolder(View itemView) {
            super(itemView);
            txtPhotoTitle =  itemView.findViewById(R.id.txt_photo_title);
            txtPhotoFilePath =  itemView.findViewById(R.id.txt_photo_file_path);
        }
    }
}
