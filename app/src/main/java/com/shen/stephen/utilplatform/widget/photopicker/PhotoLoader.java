package com.shen.stephen.utilplatform.widget.photopicker;

import android.content.Context;
import android.net.Uri;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.shen.stephen.utilplatform.widget.photopicker.entity.Photo;

import java.io.File;
import java.io.InputStream;

/**
 * Created by ChengCn on 2/4/2016.
 */
public class PhotoLoader implements ModelLoader<Photo, InputStream> {
    private final ModelLoader<Uri, InputStream> uriLoader;
    public PhotoLoader(ModelLoader<Uri, InputStream> uriLoader) {
        this.uriLoader = uriLoader;
    }
    @Override
    public DataFetcher<InputStream> getResourceFetcher(Photo model, int width, int height) {
        Uri uri;
        if (model == null) {
            return null;
        } else {
            uri = Uri.fromFile(new File(model.getPath()));
        }
        return uriLoader.getResourceFetcher(uri, width, height);
    }

    public static class Factory implements ModelLoaderFactory<Photo, InputStream> {

        @Override
        public ModelLoader<Photo, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new PhotoLoader(factories.buildModelLoader(Uri.class, InputStream.class));
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }
}
