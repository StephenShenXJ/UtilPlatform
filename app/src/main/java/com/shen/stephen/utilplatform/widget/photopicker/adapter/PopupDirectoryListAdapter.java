package com.shen.stephen.utilplatform.widget.photopicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.widget.photopicker.entity.PhotoDirectory;

import java.util.ArrayList;
import java.util.List;

public class PopupDirectoryListAdapter extends BaseAdapter {

    private Context context;

    private List<PhotoDirectory> directories = new ArrayList<>();

    private LayoutInflater mLayoutInflater;


    public PopupDirectoryListAdapter(Context context, List<PhotoDirectory> directories) {
        this.context = context;
        this.directories = directories;

        mLayoutInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return directories.size();
    }


    @Override
    public PhotoDirectory getItem(int position) {
        return directories.get(position);
    }


    @Override
    public long getItemId(int position) {
        return directories.get(position).hashCode();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.layout_photo_picker_directory_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bindData(directories.get(position));

        return convertView;
    }


    private class ViewHolder {

        public ImageView ivCover;
        public TextView tvName;
        public TextView tvCount;

        public ViewHolder(View rootView) {
            ivCover = (ImageView) rootView.findViewById(R.id.photo_picker_directory_item_image);
            tvName = (TextView) rootView.findViewById(R.id.photo_picker_directory_item_name);
            tvCount = (TextView) rootView.findViewById(R.id.photo_picker_directory_item_count);
        }

        public void bindData(PhotoDirectory directory) {
            if (context instanceof Activity && ((Activity) context).isFinishing()) {
                return;
            }
            Glide.with(context)
                    .load(directory.getCoverPath())
                    .dontAnimate()
                    .thumbnail(0.1f)
                    .into(ivCover);
            tvName.setText(directory.getName());
            tvCount.setText(String.valueOf(directory.getPhotos().size()));
        }
    }

}
