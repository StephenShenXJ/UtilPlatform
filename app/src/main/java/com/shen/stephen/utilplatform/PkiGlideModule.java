package com.shen.stephen.utilplatform;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.GlideModule;
import com.shen.stephen.utilplatform.widget.photopicker.PhotoLoader;
import com.shen.stephen.utilplatform.widget.photopicker.entity.Photo;
import com.shen.stephen.utilplatform.util.FileUtils;

import java.io.File;
import java.io.InputStream;

/**
 * Created by ChengCn on 1/20/2016.
 */
public class PkiGlideModule implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        String imageCacheDir = FileUtils.getCacheDirectory(context).getAbsolutePath() + Constants.CacheFiles.WEB_IMAGE_CACHE;
        builder.setDiskCache(new DiskCacheFactory(imageCacheDir, DiskCacheFactory.DEFAULT_DISK_CACHE_SIZE));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(Photo.class, InputStream.class, new PhotoLoader.Factory());
    }

    public static class DiskCacheFactory extends DiskLruCacheFactory {
        public static String DISK_CACHE_PATH;
        public DiskCacheFactory(String diskCacheFolder, int diskCacheSize) {
            super(diskCacheFolder, diskCacheSize);
            DISK_CACHE_PATH = diskCacheFolder;
        }

        /**
         * get the image cache size.
         *
         * @return image cache size in bytes.
         */
        public static long getCacheSize() {
            long cacheSize = 0;
            File cachedFileDir = new File(DISK_CACHE_PATH);
            if (cachedFileDir.exists() && cachedFileDir.isDirectory()) {
                File[] cachedFiles = cachedFileDir.listFiles();
                if(cachedFiles!=null){
                    for (File f : cachedFiles) {
                        if (f.exists() && f.isFile()) {
                            cacheSize += f.length();
                        }
                    }
                }
            }

            return cacheSize;
        }
    }
}
