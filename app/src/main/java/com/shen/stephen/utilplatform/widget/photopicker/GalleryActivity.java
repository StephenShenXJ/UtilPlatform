package com.shen.stephen.utilplatform.widget.photopicker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.adapter.RecyclePagerAdapter;
import com.shen.stephen.utilplatform.widget.PkiActivity;
import com.shen.stephen.utilplatform.widget.photopicker.entity.Photo;
import com.shen.stephen.utilplatform.widget.view.GalleryImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChengCn on 2/4/2016.
 */
public class GalleryActivity extends PkiActivity implements View.OnClickListener {
    public static final String IMAGES_INTENT_KEY = "com.shen.stephen.utilplatform.widget.common.IMAGES_INTENT_KEY";
    public static final String IMAGES_INTENT_PATH = "com.shen.stephen.utilplatform.widget.common.IMAGES_INTENT_PATH";
    public static final String DEFAULT_INDEX_INTENT_KEY = "com.shen.stephen.utilplatform.widget.common.DEFAULT_INDEX_INTENT_KEY";
    public static final String DEFAULT_USERNAME = "com.shen.stephen.utilplatform.widget.common.DEFAULT_USERNAME";
    public static final String IS_PHOTOPICKER = "is_photopicker";

    protected ViewPager mImageViewPager;
    protected ImageViewPagerAdapter mImageAdapter;
    protected ImageView mIconClose;

    protected ArrayList<?> mImagesInfo;
    private int mDefaultIndex;
    private ArrayList<String> mLocalPathList;
    private static boolean isPhotoPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.animator.activity_show_up_from_center_anim, R.anim.abc_popup_exit);
        super.onCreate(savedInstanceState);
        setContentView(getContentViewResId());
        init();
    }

    protected int getContentViewResId() {
        return R.layout.layout_gallery_view;
    }

    private void init() {
        mDefaultIndex = getIntent().getIntExtra(DEFAULT_INDEX_INTENT_KEY, 0);
        mImagesInfo = (ArrayList<?>) getIntent().getSerializableExtra(IMAGES_INTENT_KEY);
        mLocalPathList = getIntent().getStringArrayListExtra(IMAGES_INTENT_PATH);
        isPhotoPicker = getIntent().getBooleanExtra(IS_PHOTOPICKER, false);

        mImageAdapter = new ImageViewPagerAdapter(this, mImagesInfo, mLocalPathList);
        mImageViewPager = (ViewPager) findViewById(R.id.gallery_image_view_main);
        mImageViewPager.setAdapter(mImageAdapter);
        mImageViewPager.setCurrentItem(mDefaultIndex);

        mIconClose = (ImageView) findViewById(R.id.gallery_image_view_close_icon);
        mIconClose.setOnClickListener(this);

    }

    protected void setDefaultIndex(int index) {
        mImageViewPager.setCurrentItem(index);
    }

    protected void setImagesInfo(ArrayList<?> imagesInfo) {
        mImagesInfo = imagesInfo;
        if (mImageAdapter != null) {
            mImageAdapter.setImagesInfo(mImagesInfo);
        }
    }

    protected void removeImageByIndex(int index) {
        mImagesInfo.remove(index);
    }

    @Override
    public void onClick(View v) {
        if (v == mIconClose) {
            finish();
        }
    }

    private static class ImageViewPagerAdapter extends RecyclePagerAdapter {
        private Context mContext;
        private List<?> mImages;
        private List<String> mPaths;
        private LayoutInflater mInflater;

        ImageViewPagerAdapter(Context context, List<?> instrumentImages, List<String> localpath) {
            mContext = context;
            mImages = instrumentImages;
            mPaths = localpath;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setImagesInfo(List<?> images) {
            mImages = images;
            notifyDataSetChanged();
        }

        @Override
        protected View getItemView(int position) {
            View view = mInflater.inflate(R.layout.layout_gallery_image_item, null, false);
            return view;
        }

        @Override
        protected void bindItemView(View itemView, int position, boolean isReused) {
            GalleryImageView imageView = (GalleryImageView) itemView.findViewById(R.id.gallery_image_item_imageView);
            Object image = mImages.get(position);
            String path;
            if (mPaths != null && mPaths.size() > 0) {
                path = mPaths.get(position);
            } else {
                path = "";
            }


            ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.gallery_image_item_progressbar);
            progressBar.setVisibility(View.VISIBLE);
            if (!isPhotoPicker) {
                Glide.with(mContext).load(image).placeholder(R.drawable.gallery_placeholder).error(R.drawable.gallery_error)
                        .diskCacheStrategy(image instanceof Photo ? DiskCacheStrategy.NONE : DiskCacheStrategy.RESULT)
                        .listener(new ImageRequestListener(progressBar)).into(imageView);
            } else {
                Glide.with(mContext).load(image).placeholder(R.drawable.gallery_placeholder).error(R.drawable.gallery_error)
                        .diskCacheStrategy(image instanceof Photo ? DiskCacheStrategy.NONE : DiskCacheStrategy.RESULT)
                        .listener(new ImageRequestListener(progressBar)).into(imageView);
            }


        }

        @Override
        public int getCount() {
            if (mImages == null) {
                return 0;
            }
            return mImages.size();
        }
    }

    private static class ImageRequestListener implements RequestListener<Object, GlideDrawable> {

        public ProgressBar mProgressBar;

        public ImageRequestListener(ProgressBar progressBar) {
            mProgressBar = progressBar;
        }

        @Override
        public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
            mProgressBar.setVisibility(View.GONE);
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            mProgressBar.setVisibility(View.GONE);
            return false;
        }
    }
}
