package com.shen.stephen.utilplatform.widget.photopicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.widget.photopicker.adapter.PhotoGridAdapter;
import com.shen.stephen.utilplatform.widget.photopicker.adapter.PopupDirectoryListAdapter;
import com.shen.stephen.utilplatform.widget.photopicker.entity.Photo;
import com.shen.stephen.utilplatform.widget.photopicker.entity.PhotoDirectory;
import com.shen.stephen.utilplatform.widget.photopicker.event.OnItemCheckListener;
import com.shen.stephen.utilplatform.widget.photopicker.event.OnPhotoClickListener;
import com.shen.stephen.utilplatform.widget.PkiActivity;
import com.shen.stephen.utilplatform.widget.PkiFragment;
import com.shen.stephen.utilplatform.util.FileUtils;
import com.shen.stephen.utilplatform.util.PicturesUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChengCn on 1/19/2016.
 */
public class PhotoPickerFragment extends PkiFragment {

    public static final String PHOTO_PICKER_RESULT_KEY = "com.shen.stephen.utilplatform.widget.common.PhotoPicker.PHOTO_PICKER_RESULT_KEY";
    public static final int REQUEST_TAKE_PHOTO = 1;
    private PhotoGridAdapter mPhotoGridAdapter;

    private PopupDirectoryListAdapter mAlbumListAdapter;
    private List<PhotoDirectory> directories;

    private int SCROLL_THRESHOLD = 30;

    private ListPopupWindow mAlbumSelectWindow;
    private TextView mAllImageTextView;

    private String mCaptureImagePath;
    private Uri mCaptureImageUri;
    private MenuItem mUploadMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        directories = new ArrayList<>();
        setHasOptionsMenu(true);
        setRetainInstance(true);
        MediaStoreHelper.getPhotoDirs((PkiActivity) getActivity(), null,
                new MediaStoreHelper.PhotosResultCallback() {
                    @Override
                    public void onResultCallback(List<PhotoDirectory> dirs) {
                        directories.clear();
                        directories.addAll(dirs);
                        if (mPhotoGridAdapter.selectPhoto(mCaptureImagePath, true)) {
                            mUploadMenu.setEnabled(true);
                            mUploadMenu.setVisible(true);
                            mCaptureImagePath = null;
                            mCaptureImageUri = null;
                        }

                        mPhotoGridAdapter.notifyDataSetChanged();
                        mAlbumListAdapter.notifyDataSetChanged();
                    }
                });
        mPhotoGridAdapter = new PhotoGridAdapter(getActivity(), directories);
        mAlbumListAdapter = new PopupDirectoryListAdapter(getActivity(), directories);
        mPhotoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {
            @Override
            public void onClick(View v, int position, boolean showCamera) {
                openImageGallery(showCamera ? --position : position);
            }
        });
        mPhotoGridAdapter.setOnCameraClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCaptureImageUri = PicturesUtil.takePictureByCamera(PhotoPickerFragment.this, REQUEST_TAKE_PHOTO);
                mCaptureImagePath = FileUtils.getPath(getContext(), mCaptureImageUri);
            }
        });
        mPhotoGridAdapter.setOnItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean OnItemCheck(int position, Photo photo, boolean isCheck, int totalSelectedCount) {
                if (!isCheck && mPhotoGridAdapter.getSelectedItemCount() == 1) {
                    mUploadMenu.setEnabled(false);
                    mUploadMenu.setVisible(false);
                } else {
                    mUploadMenu.setEnabled(true);
                    mUploadMenu.setVisible(true);
                }
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photo_picker_menu, menu);
        mUploadMenu = menu.findItem(R.id.photo_picker_action_done);

        boolean haveSelectedPhoto = mPhotoGridAdapter == null ? false : mPhotoGridAdapter.getSelectedItemCount() > 0;
        mUploadMenu.setEnabled(haveSelectedPhoto);
        mUploadMenu.setVisible(haveSelectedPhoto);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.photo_picker_action_done) {
            Intent i = new Intent();
            i.putStringArrayListExtra(PHOTO_PICKER_RESULT_KEY, mPhotoGridAdapter.getSelectedPhotoPaths());
            setResult(Activity.RESULT_OK, i);
            onBackClicked();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // delete the tmp file.
                new File(mCaptureImagePath).delete();
                mPhotoGridAdapter.selectPhoto(mCaptureImagePath, false);
                mCaptureImagePath = null;
            } else {
                PicturesUtil.galleryAddPic(getContext(), mCaptureImageUri);
            }
        }
    }

    @Override
    protected int getContentViewResourceId() {
        return R.layout.fragment_photo_picker_layout;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.photo_picker_image_list_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mPhotoGridAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > SCROLL_THRESHOLD) {
                    Glide.with(getActivity()).pauseRequests();
                } else {
                    Glide.with(getActivity()).resumeRequests();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(getActivity()).resumeRequests();
                }
            }
        });

        mAllImageTextView = (TextView) findViewById(R.id.photo_picker_all_image_label);
        mAllImageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAlbumSelectWindow.isShowing()) {
                    mAlbumSelectWindow.dismiss();
                } else if (!getActivity().isFinishing()) {
                    mAlbumSelectWindow.setHeight(Math.round(mContentView.getHeight() * 0.8f));
                    mAlbumSelectWindow.show();
                }
            }
        });

        createAlbumSelectWindow();
    }

    private void createAlbumSelectWindow() {
        if (mAlbumSelectWindow == null) {
            mAlbumSelectWindow = new ListPopupWindow(getActivity());
            mAlbumSelectWindow.setWidth(ListPopupWindow.MATCH_PARENT);
            mAlbumSelectWindow.setBackgroundDrawable(getContext().getResources().getDrawable(R.color.white));
            mAlbumSelectWindow.setAnchorView(mAllImageTextView);
            mAlbumSelectWindow.setAdapter(mAlbumListAdapter);
            mAlbumSelectWindow.setModal(true);
            mAlbumSelectWindow.setDropDownGravity(Gravity.BOTTOM);
            mAlbumSelectWindow.setAnimationStyle(R.style.Animation_AppCompat_DropDownUp);

            mAlbumSelectWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAlbumSelectWindow.dismiss();

                    PhotoDirectory directory = directories.get(position);

                    mAllImageTextView.setText(directory.getName());

                    mPhotoGridAdapter.setCurrentDirectoryIndex(position);
                    mPhotoGridAdapter.notifyDataSetChanged();
                }
            });
        } else {
            mAlbumSelectWindow.setAnchorView(mAllImageTextView);
        }
    }

    private void openImageGallery(int position) {
        Intent i = new Intent(getContext(), GalleryActivity.class);
        i.putExtra(GalleryActivity.DEFAULT_INDEX_INTENT_KEY, position);
        i.putExtra(GalleryActivity.IS_PHOTOPICKER, true);
        i.putExtra(GalleryActivity.IMAGES_INTENT_KEY, mPhotoGridAdapter.getCurrentPhotos());
        startActivity(i);
    }

    public PhotoGridAdapter getPhotoGridAdapter() {
        return mPhotoGridAdapter;
    }

    public ArrayList<String> getSelectedPhotoPaths() {
        return mPhotoGridAdapter.getSelectedPhotoPaths();
    }
}
