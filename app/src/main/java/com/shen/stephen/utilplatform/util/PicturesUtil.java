package com.shen.stephen.utilplatform.util;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.log.PLog;
import com.shen.stephen.utilplatform.widget.PkiFragment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * this class is used to get pictures for a new request
 */
public class PicturesUtil {
    private static final String[] imageFileExtensions = new String[]{"jpg", "png", "gif", "jpeg"};

    public static boolean isImage(File file) {
        if (file == null) {
            return false;
        }

        for (String extension : imageFileExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    // take the pictures from camera
    public static Uri takePictureByCamera(PkiFragment fragment, int requestCode) {
        return takePictureByCamera(fragment.getActivity(), fragment, requestCode);
    }

    public static Uri takePictureByCamera(Activity activity, PkiFragment fragment, int requestCode) {
        Uri imageUri = null;
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Context context = activity == null ? fragment.getContext() : activity;
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = createImageFile();
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    imageUri = Uri.fromFile(photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                }
            }
            if (fragment != null) {
                fragment.startActivityForResult(takePictureIntent, requestCode);
            } else {
                activity.startActivityForResult(takePictureIntent, requestCode);
            }
        } catch (Exception e) {
            PLog.i("Can't take photo.", e);
        }

        return imageUri;
    }

    private static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            if (!storageDir.mkdir()) {
                throw new IOException();
            }
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    private static File createThumbnailImageFile(String name) throws IOException {
        // Create an image file name
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            if (!storageDir.mkdir()) {
                throw new IOException();
            }
        }
        File thumbnailDir = new File(storageDir, ".thumbnail");
        if (!thumbnailDir.exists())
            thumbnailDir.mkdir();
        File image = new File(thumbnailDir, name);
        return image;
    }

    public static void galleryAddPic(Context context, Uri photoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(photoPath);
        context.sendBroadcast(mediaScanIntent);
    }

    public static Bitmap loadPictures(Context context, Uri imageUri) {
        String filePath = null;
        Bitmap bitmap = null;
        if (imageUri != null) {
            try {
                filePath = FileUtils.getPath(context, imageUri);
                PLog.v("filePath = " + filePath);
                if (filePath != null) {
                    bitmap = decodeImageFile(filePath);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * Load a compressed picture, that match the specified size.
     *
     * @param context        the context.
     * @param imageUri       the URI of the picture.
     * @param size           the expected size of the picture in pixel. If height > width the size will be as the expected picture's height
     * @param isProportional specify whether equal proportional compress picture or not, if set false the height and width will compressed as specified size.
     * @return
     */
    public static Bitmap loadCompressedPicture(Context context, Uri imageUri, int size, boolean isProportional) {
        Bitmap bitmap = null;
        String filePath;
        if (imageUri != null) {
            try {
                filePath = FileUtils.getPath(context, imageUri);
                PLog.v("filePath = " + filePath);
                if (filePath != null) {
                    bitmap = decodeCompressedImageFile(filePath, size, isProportional);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static boolean isAttachmentExceedsLimit(long imageSize, long currentSize) {
        boolean isExceeded = false;
        long totalSize = currentSize;
        totalSize += imageSize;
        if (totalSize > (20 * 1024 * 1024)) {
            isExceeded = true;
        }
        return isExceeded;
    }

    public static long getSizeAttachment(Context context, Uri imageUri) {
        long size;
        File file = new File(FileUtils.getPath(context, imageUri));
        size = file.length();
        return size;
    }

    /**
     * Asynchronous load bit map from local extra storage.
     *
     * @param context   the context.
     * @param imageView the image view that will be display the bit map.
     * @param uri       the uri of the bit map
     */
    public static void asyncLoadBitmap(Context context, ImageView imageView, boolean isScale, Uri uri) {
        if (imageView == null) {
            return;
        }

        int desirHeight = imageView.getMeasuredHeight();
        asyncLoadBitmap(context, imageView, desirHeight, uri, isScale, null);
    }

    /**
     * Asynchronous load bit map from local extra storage.
     *
     * @param context    the context.
     * @param imageView  the image view that will be display the bit map.
     * @param desireSize the desire size of the bit map.
     * @param uri        the uri of the bit map
     * @param cache      the image cache.
     */
    public static void asyncLoadBitmap(Context context, ImageView imageView,
                                       int desireSize, Uri uri, boolean isScale, LruCache<Uri, Bitmap> cache) {
        if (cache != null && uri != null) {
            Bitmap bitmap = cache.get(uri);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        }

        if (cancelPotentialWork(uri, imageView)) {
            final LoadImageAsyncTask task = new LoadImageAsyncTask(context,
                    imageView, desireSize, isScale, cache);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(
                    context.getResources(), task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(uri);
        }
    }

    public static String encodeBitmap2Base64(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        try {
            // First to convert the bitmap to a byte array average
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
            byte[] buffer = out.toByteArray();
            // The byte array into Base64 string
            String imageEncoded = Base64.encodeToString(buffer, Base64.DEFAULT);

            return imageEncoded;
        } catch (Exception e) {
            e.printStackTrace();
            PLog.i("image encoding Exception = " + e.toString());
        }
        return null;
    }

    public static Matrix getAjustingMatrix(String filePath) {
        Matrix matrix = new Matrix();
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            if (rotation != 0f) {
                matrix.preRotate(rotationInDegrees);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }

    public static Bitmap decodeImageFile(String filePath) {
        Matrix matrix;
        Bitmap bitmap;
        if (filePath == null) {
            return null;
        } else {

            matrix = getAjustingMatrix(filePath);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, o);

            DisplayMetrics dm = DeviceUtils.getScreenSize();
            o.inSampleSize = calculateInSampleSize(o, dm.widthPixels, dm.heightPixels);
            // Decode with inSampleSize
            o.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filePath, o);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
        }
    }

    /**
     * @param filePath the image's file path
     * @param size     the size of the
     * @param isScale  specify whether is Scaling or not
     * @return
     */
    public static Bitmap decodeCompressedImageFile(String filePath, int size, boolean isScale) {
        if (StrUtil.isEmpty(filePath)) {
            return null;
        }

        Bitmap bitmap;
        Bitmap ret;
        Matrix matrix = getAjustingMatrix(filePath);
        DisplayMetrics dm = DeviceUtils.getScreenSize();

        BitmapFactory.Options o = new BitmapFactory.Options();
        if (!isScale) {
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, o);

            o.inJustDecodeBounds = false;
            o.inSampleSize = calculateInSampleSize(o, dm.widthPixels, dm.heightPixels);
            bitmap = BitmapFactory.decodeFile(filePath, o);
            if (bitmap == null) {
                return null;
            }

            ret = cropImageBitmap(bitmap, size, matrix);
            bitmap.recycle();
            return ret;
        } else {
            int desiredWidth;
            int desiredHeight;

            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, o);

            if (size < o.outWidth && size < o.outHeight) {
                boolean isWidth = o.outWidth > o.outHeight;
                if (isWidth) {
                    desiredHeight = (int) (((float) o.outHeight * size) / o.outWidth);
                    desiredWidth = size;
                } else {
                    desiredHeight = size;
                    desiredWidth = (int) (((float) o.outWidth * size) / o.outHeight);
                }
                o.inSampleSize = calculateInSampleSize(o, desiredWidth, desiredHeight);
            } else {
                o.inSampleSize = 1;
            }

            o.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filePath, o);

            if (bitmap == null) {
                return null;
            }

            Bitmap adjustBitmap = bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return adjustBitmap;
        }
    }

    public static Bitmap resizeImageBitmap(Bitmap bm, int size, boolean isWidth) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        if (isWidth) {
            height = (int) (((float) height * size) / width);
            width = size;

        } else {
            width = (int) (((float) width * size) / height);
            height = size;
        }

        return Bitmap.createScaledBitmap(bm, width, height, true);
    }

    public static Bitmap cropImageBitmap(Bitmap bm, int size, Matrix matrix) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        int x = 0;
        int y = 0;
        boolean isWidth = (width > height) ? true : false;
        if (isWidth) {
            width = (int) (((float) width * size) / height);
            height = size;
            x = (width - size) / 2;
            y = 0;
        } else {
            height = (int) (((float) height * size) / width);
            width = size;
            y = (height - size) / 2;
            x = 0;
        }
        Bitmap nb = Bitmap.createScaledBitmap(bm, width, height, true);
        return Bitmap.createBitmap(nb, x, y, size, size, matrix, true);

    }

    public static Bitmap cropImageBitmap(Bitmap bm, int size) {
        return cropImageBitmap(bm, size, null);
    }

    public static Bitmap FillupImageBitmap(Bitmap bm, int size) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        boolean isWidth = (width > height) ? true : false;
        if (isWidth) {
            height = (int) (((float) height * size) / width);
            width = size;

        } else {
            width = (int) (((float) width * size) / height);
            height = size;
        }

        Bitmap nb = Bitmap.createScaledBitmap(bm, width, height, true);
        return nb;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // BEGIN_INCLUDE (calculate_sample_size)
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / (inSampleSize * inSampleSize);

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        return inSampleSize;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private static boolean cancelPotentialWork(Uri uri, ImageView imageView) {
        final LoadImageAsyncTask bitmapWorkerTask = getBitmapLoadTask(imageView);

        if (bitmapWorkerTask != null) {
            final Uri uriData = bitmapWorkerTask.uri;
            // If bitmapData is not yet set or it differs from the new data
            if (uri == null || !uri.equals(uriData)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
    }

    private static LoadImageAsyncTask getBitmapLoadTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapLoadTask();
            }
        }
        return null;
    }

    private static class LoadImageAsyncTask extends
            AsyncTask<Uri, Void, Bitmap> {
        private Context mContext;
        private int mImageHeight;
        private boolean mIsScale;
        private Uri uri;
        private WeakReference<ImageView> mImageViewRef;
        private LruCache<Uri, Bitmap> mCache;

        LoadImageAsyncTask(Context context, ImageView imageView,
                           int imageHeight, boolean isScale, LruCache<Uri, Bitmap> cache) {
            mContext = context;
            mImageHeight = imageHeight;
            mImageViewRef = new WeakReference<ImageView>(imageView);
            mCache = cache;
            mIsScale = isScale;
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            uri = params[0];
            if (uri == null) {
                return null;
            }

            Bitmap ret = PicturesUtil.loadCompressedPicture(mContext, uri, mImageHeight, mIsScale);

            return ret;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (mImageViewRef != null && bitmap != null) {
                final ImageView imageView = mImageViewRef.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }

                if (mCache != null) {
                    mCache.put(uri, bitmap);
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<LoadImageAsyncTask> bitmapLoadTaskReference;

        public AsyncDrawable(Resources res, LoadImageAsyncTask bitmapLoadTask) {
            super(res, (Bitmap) null);
            bitmapLoadTaskReference = new WeakReference<LoadImageAsyncTask>(
                    bitmapLoadTask);
        }

        public LoadImageAsyncTask getBitmapLoadTask() {
            return bitmapLoadTaskReference.get();
        }
    }

    public static File getThumbnail(String path, String name) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        File lastFile = createThumbnailImageFile(name);
        int size = 2;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = size;
        Bitmap image = BitmapFactory.decodeStream(fis, null, options);
        fis.close();

        if (image != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            int per = 100;
            while (baos.toByteArray().length / 1024 > 20 && per > 5) { // finally biggest size 20kb
                baos.reset();
                image.compress(Bitmap.CompressFormat.JPEG, per, baos);
                per -= 5;
            }
            if (image != null && !image.isRecycled()) {
                image.recycle();
                image = null;
                System.gc();
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            baos.close();
            FileOutputStream fos = new FileOutputStream(lastFile);
            FileUtils.copyStream(bais, fos);
            bais.close();
            fos.close();
            return lastFile;
        } else
            return null;
    }

    public static ImageView initDialog(Context mContext) {
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        View mLayout = mInflater
                .inflate(R.layout.dialog_image_fullscreen, null);

        final Dialog dialog = new Dialog(mContext, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mLayout);

        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        ImageView imageLarge = (ImageView) dialog
                .findViewById(R.id.imageview_fullscreen);
        ImageButton buttonClose = (ImageButton) dialog
                .findViewById(R.id.button_close);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        return imageLarge;
    }

    public static void imageFullScreenMode(Bitmap bitmap, Context mContext) {
        ImageView imageLarge = initDialog(mContext);

        if (bitmap != null) {
            imageLarge.setImageBitmap(bitmap);
        }
    }

    public static String getBase64Str(File file) {
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            int length = in.read(bytes);
            base64 = Base64.encodeToString(bytes, 0, length, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return base64;
    }

}
