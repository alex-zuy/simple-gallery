package com.example.alex.simplegallery;

import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

public class DirectoryDataSource implements DataSource {

    private final File directory;

    private final DisplayMetrics displayMetrics;

    private final List<File> files;

    private int currentFileIndex;

    private BitmapConsumer consumer;

    private LocalImageLoadTask lastTask;

    public DirectoryDataSource(final File directory, final DisplayMetrics displayMetrics) {
        this.directory = directory;
        this.displayMetrics = displayMetrics;
        files = getImagesInDirectory(directory);
    }

    @Override
    public void prepareNextImage() {
        lastTask = new LocalImageLoadTask(displayMetrics, consumer);
        currentFileIndex = (currentFileIndex + 1) % files.size();
        lastTask.execute(files.get(currentFileIndex));
    }

    @Override
    public void setConsumer(final BitmapConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void setLoadProgressListener(LoadProgressListener progressListener) {
        // we will not report progress
    }

    @Override
    public boolean hasNextImage() {
        return !files.isEmpty();
    }

    @Override
    public void destroy() {
        if(lastTask != null) {
            lastTask.cancel(true);
        }
    }

    private static List<File> getImagesInDirectory(final File directory) {
        return Arrays.asList(directory.listFiles(new ImageFilesFilter()));
    }

    private static class ImageFilesFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return pathname.canRead() && pathname.isFile() && isImage(pathname);
        }

        private boolean isImage(final File file) {
            final Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            return options.outWidth != -1 && options.outHeight != -1;
        }
    }
}
