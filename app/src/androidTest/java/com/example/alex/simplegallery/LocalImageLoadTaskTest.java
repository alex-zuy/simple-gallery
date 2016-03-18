package com.example.alex.simplegallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.DisplayMetrics;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LocalImageLoadTaskTest {

    private final Context context = InstrumentationRegistry.getContext();

    private final Executor executor = new SingleThreadExecutor();

    @Test
    public void testLoadBigImage() throws Exception {
        tryLoadAssetFile("test_image_big.jpeg");
    }

    @Test
    public void testLoadSmallImage() throws Exception {
        tryLoadAssetFile("test_image_small.jpeg");
    }

    private void tryLoadAssetFile(final String assetFileName) throws IOException {
        final InputStream in = context.getResources().getAssets().open(assetFileName);
        final File file = new File(context.getExternalCacheDir(), "image");
        final FileOutputStream out = new FileOutputStream(file);
        IOUtils.copy(in, out);
        in.close();
        out.close();
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final AsyncTask<File, Integer, Bitmap> loadFirstImage =
            new LocalImageLoadTask(displayMetrics, new BitmapConsumer() {
            @Override
            public void consume(Bitmap bitmap) {
                assertNotNull(bitmap);
            }
        });
        loadFirstImage.executeOnExecutor(executor, file);
    }

    private static class SingleThreadExecutor implements Executor {

        @Override
        public void execute(Runnable runnable) {
            runnable.run();
        }
    }
}
