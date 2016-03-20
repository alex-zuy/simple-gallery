package com.example.alex.simplegallery;

import android.content.Context;
import android.os.AsyncTask;
import android.util.DisplayMetrics;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;

import autovalue.shaded.com.google.common.common.collect.Lists;

public class UrlListDataSource implements DataSource {

    private final Context context;

    private final DisplayMetrics displayMetrics;

    private final List<URL> urls;

    private DownloadImageTask lastTask;

    private int currentUrlIndex;

    private BitmapConsumer consumer;

    private LoadProgressListener progressListener;

    public UrlListDataSource(final Context context, final DisplayMetrics displayMetrics, final Set<URL> urls) {
        this.context = context;
        this.displayMetrics = displayMetrics;
        this.urls = Lists.newArrayList(urls);
        setLoadProgressListener(null);
    }

    @Override
    public void setConsumer(final BitmapConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void setLoadProgressListener(final LoadProgressListener progressListener) {
        if(progressListener != null) {
            this.progressListener = progressListener;
        }
        else {
            this.progressListener = new NoOpLoadProgressListener();
        }
    }

    @Override
    public void prepareNextImage() {
        lastTask = new DownloadImageTask(context, new FileLoadedCallback(), progressListener);
        currentUrlIndex = (currentUrlIndex + 1) % urls.size();
        lastTask.execute(urls.get(currentUrlIndex));
    }

    @Override
    public boolean hasNextImage() {
        return !urls.isEmpty();
    }

    @Override
    public void destroy() {
        if(lastTask != null) {
            lastTask.cancel(true);
        }
    }

    private class FileLoadedCallback implements DownloadedFileConsumer {

        @Override
        public void consume(final File file) {
            final LocalImageLoadTask task = new LocalImageLoadTask(displayMetrics, consumer);
            task.execute(file);
        }
    }
}
