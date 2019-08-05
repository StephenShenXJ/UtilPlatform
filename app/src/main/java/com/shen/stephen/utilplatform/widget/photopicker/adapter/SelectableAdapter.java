package com.shen.stephen.utilplatform.widget.photopicker.adapter;

import android.support.v7.widget.RecyclerView;


import com.shen.stephen.utilplatform.widget.photopicker.entity.Photo;
import com.shen.stephen.utilplatform.widget.photopicker.entity.PhotoDirectory;
import com.shen.stephen.utilplatform.widget.photopicker.event.Selectable;

import java.util.ArrayList;
import java.util.List;


public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements Selectable {

    private static final String TAG = SelectableAdapter.class.getSimpleName();

    protected List<PhotoDirectory> photoDirectories;
    protected List<Photo> selectedPhotos;
    public int currentDirectoryIndex = 0;


    public SelectableAdapter() {
        photoDirectories = new ArrayList<>();
        selectedPhotos = new ArrayList<>();
    }


    /**
     * Indicates if the item at position position is selected
     *
     * @param photo Photo of the item to check
     * @return true if the item is selected, false otherwise
     */
    @Override
    public boolean isSelected(Photo photo) {
        return getSelectedPhotos().contains(photo);
    }

    /**
     * Get the photo whose path is equal the specified path
     *
     * @param photoPath the path of the photo
     */
    public Photo getPhoto(String photoPath) {
        List<Photo> photos = getCurrentPhotos();
        if (photos == null) {
            return null;
        }

        for (Photo p : photos) {
            if (p.getPath().equals(photoPath)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param photo Photo of the item to toggle the selection status for
     */
    @Override
    public void toggleSelection(Photo photo) {
        if (selectedPhotos.contains(photo)) {
            selectedPhotos.remove(photo);
        } else {
            selectedPhotos.add(photo);
        }
    }


    /**
     * Clear the selection status for all items
     */
    @Override
    public void clearSelection() {
        selectedPhotos.clear();
    }


    /**
     * Count the selected items
     *
     * @return Selected items count
     */
    @Override
    public int getSelectedItemCount() {
        return selectedPhotos.size();
    }


    public void setCurrentDirectoryIndex(int currentDirectoryIndex) {
        this.currentDirectoryIndex = currentDirectoryIndex;
    }


    public ArrayList<Photo> getCurrentPhotos() {
        return photoDirectories.get(currentDirectoryIndex).getPhotos();
    }

    @Override
    public List<Photo> getSelectedPhotos() {
        return selectedPhotos;
    }

}