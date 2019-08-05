package com.shen.stephen.utilplatform.widget.photopicker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.util.StrUtil;
import com.shen.stephen.utilplatform.widget.photopicker.MediaStoreHelper;
import com.shen.stephen.utilplatform.widget.photopicker.entity.Photo;
import com.shen.stephen.utilplatform.widget.photopicker.entity.PhotoDirectory;
import com.shen.stephen.utilplatform.widget.photopicker.event.OnItemCheckListener;
import com.shen.stephen.utilplatform.widget.photopicker.event.OnPhotoClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by donglua on 15/5/31.
 */
public class PhotoGridAdapter extends SelectableAdapter<PhotoGridAdapter.PhotoViewHolder> {

    private LayoutInflater inflater;

    private Context mContext;

    private OnItemCheckListener onItemCheckListener = null;
    private OnPhotoClickListener onPhotoClickListener = null;
    private View.OnClickListener onCameraClickListener = null;

    public final static int ITEM_TYPE_CAMERA = 100;
    public final static int ITEM_TYPE_PHOTO = 101;

    private boolean hasCamera = true;

    private final int imageSize;

    private int totalNumber;

    public PhotoGridAdapter(Context context, List<PhotoDirectory> photoDirectories) {
        this.photoDirectories = photoDirectories;
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;

        imageSize = widthPixels / 3;
    }


    @Override
    public int getItemViewType(int position) {
        return (showCamera() && position == 0) ? ITEM_TYPE_CAMERA : ITEM_TYPE_PHOTO;
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.layout_photo_picker_item, parent, false);
        PhotoViewHolder holder = new PhotoViewHolder(itemView);
        if (viewType == ITEM_TYPE_CAMERA) {
            holder.checkBox.setVisibility(View.GONE);
            holder.photoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onCameraClickListener != null) {
                        onCameraClickListener.onClick(view);
                    }
                }
            });
        }
        return holder;
    }

    /**
     * Select or deselect a photo.
     *
     * @param photoPath  the path of the photo.
     * @param isSelected specify whether is select or deselect. true is for select, false is for deselect.
     * @return true if select or deselect the photo. if the photo is already selected or deselected return false.
     */
    public boolean selectPhoto(String photoPath, boolean isSelected) {
        if (StrUtil.isEmpty(photoPath)) {
            return false;
        }

        Photo photo = getPhoto(photoPath);
        if (photo == null) {
            return false;
        }
        if (!selectedPhotos.contains(photo) && isSelected) {
            selectedPhotos.add(photo);
            return true;
        } else if (selectedPhotos.contains(photo) && !isSelected) {
            selectedPhotos.remove(photo);
            return true;
        }

        return false;
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

        if (getItemViewType(position) == ITEM_TYPE_PHOTO) {

            List<Photo> photos = getCurrentPhotos();
            final Photo photo;

            if (showCamera()) {
                photo = photos.get(position - 1);
            } else {
                photo = photos.get(position);
            }

            Glide.with(mContext.getApplicationContext())
                    .load(new File(photo.getPath()))
                    .centerCrop()
                    .dontAnimate()
                    .thumbnail(0.5f)
                    .override(imageSize, imageSize)
                    .placeholder(R.drawable.photo_picker_placeholder)
                    .error(R.drawable.photo_picker_placeholder).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.photoImageView);

            final boolean isChecked = isSelected(photo);
            holder.checkBox.setSelected(isChecked);

            holder.photoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onPhotoClickListener != null) {
                        onPhotoClickListener.onClick(view, position, showCamera());
                    }
                }
            });
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    boolean isEnable = true;

                    if (onItemCheckListener != null) {
                        isEnable = onItemCheckListener.OnItemCheck(position, photo, !isChecked,
                                getSelectedPhotos().size());
                    }
                    if (isEnable) {
                        if (selectedPhotos.contains(photo) || totalNumber < 10) {
                            if (selectedPhotos.contains(photo))
                                totalNumber--;
                            else
                                totalNumber++;
                            toggleSelection(photo);
                            notifyItemChanged(position);
                        } else
                            Toast.makeText(mContext, mContext.getString(R.string.instrument_image_total_number), Toast.LENGTH_LONG).show();
                    }
                }
            });

        } else {
            holder.photoImageView.setImageResource(R.drawable.photo_picker_camera_selector);
        }
    }


    @Override
    public int getItemCount() {
        int photosCount =
                photoDirectories.size() == 0 ? 0 : getCurrentPhotos().size();
        if (showCamera()) {
            return photosCount + 1;
        }
        return photosCount;
    }


    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView photoImageView;
        private ImageView checkBox;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            photoImageView = (ImageView) itemView.findViewById(R.id.photo_picker_item_image);
            checkBox = (ImageView) itemView.findViewById(R.id.photo_picker_item_check);
        }
    }


    public void setOnItemCheckListener(OnItemCheckListener onItemCheckListener) {
        this.onItemCheckListener = onItemCheckListener;
    }


    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        this.onPhotoClickListener = onPhotoClickListener;
    }


    public void setOnCameraClickListener(View.OnClickListener onCameraClickListener) {
        this.onCameraClickListener = onCameraClickListener;
    }


    public ArrayList<String> getSelectedPhotoPaths() {
        ArrayList<String> selectedPhotoPaths = new ArrayList<>(getSelectedItemCount());

        for (Photo photo : selectedPhotos) {
            selectedPhotoPaths.add(photo.getPath());
        }

        return selectedPhotoPaths;
    }


    public void setShowCamera(boolean hasCamera) {
        this.hasCamera = hasCamera;
    }


    public boolean showCamera() {
        return (hasCamera && currentDirectoryIndex == MediaStoreHelper.INDEX_ALL_PHOTOS);
    }
}
