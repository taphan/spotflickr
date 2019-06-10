package com.cs550.teama.spotflickr.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.cs550.teama.spotflickr.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageListAdapter extends ArrayAdapter {
    private Context context;
    private LayoutInflater inflater;

    private List<String> imageUrls;

    public ImageListAdapter(Context context, List<String> imageUrls) {
        super(context, R.layout.listview_item_image, imageUrls);

        this.context = context;
        this.imageUrls = imageUrls;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.listview_item_image, parent, false);
        }
        Picasso
                .with(context)
                .load(imageUrls.get(position))
                .resize(parent.getWidth(), 0)
                .into((ImageView) convertView);

        return convertView;
    }

}