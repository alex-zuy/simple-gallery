package com.example.alex.simplegallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.util.DisplayMetrics;

import java.io.File;

public class LocalImageLoadTask extends AsyncTask<File, Integer, Bitmap> {

    private final BitmapConsumer consumer;

    private final DisplayMetrics displayMetrics;

    public LocalImageLoadTask(final DisplayMetrics displayMetrics, final BitmapConsumer consumer) {
        this.consumer = consumer;
        this.displayMetrics = displayMetrics;
    }

    @Override
    protected Bitmap doInBackground(final File... params) {
        final File file = params[0];
        return loadSampledBitmap(file);
    }

    private Bitmap loadSampledBitmap(final File file) {
        final Options decodeOptions = new Options();
        decodeOptions.inSampleSize = getSampleSize(getImageProperties(file), displayMetrics);
        return BitmapFactory.decodeFile(file.getAbsolutePath(), decodeOptions);
    }

    @Override
    protected void onPostExecute(final Bitmap bitmap) {
        consumer.consume(bitmap);
    }

    private static int getSampleSize(final Options image, final DisplayMetrics display) {
        final int imageHeight = image.outHeight;
        final int imageWidth = image.outWidth;
        int inSampleSize = 1;
        if (imageHeight > display.heightPixels || imageWidth > display.widthPixels) {
            final int halfHeight = imageHeight / 2;
            final int halfWidth = imageWidth / 2;
            while ((halfHeight / inSampleSize) > display.heightPixels
                    && (halfWidth / inSampleSize) > display.widthPixels) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Options getImageProperties(final File file) {
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return options;
    }
}
