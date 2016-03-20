package com.example.alex.simplegallery;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadImageTask extends AsyncTask<URL, Integer, File> {

    private final static String CONTENT_LENGTH_HEADER = "Content-Length";

    private final static int DOWNLOAD_STARTED_PROGRESS = -1;

    private final static int DOWNLOAD_FINISHED_PROGRESS = 101;

    private final static int BUFFER_LENGTH = 4 * 1024;

    private final Context context;

    private final DownloadedFileConsumer consumer;

    private final LoadProgressListener listener;

    private Throwable throwable;

    public DownloadImageTask(final Context context, final DownloadedFileConsumer consumer, final LoadProgressListener listener) {
        this.context = context;
        this.consumer = consumer;
        this.listener = listener;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        final int value = values[0];
        if(value == DOWNLOAD_STARTED_PROGRESS) {
            listener.progressStarted();
        }
        else if(value == DOWNLOAD_FINISHED_PROGRESS) {
            listener.progressCompleted();
        }
        else {
            listener.progressUpdated(value);
        }
    }

    @Override
    protected void onPostExecute(File file) {
        if(throwable == null) {
            consumer.consume(file);
        }
        else {
            listener.errorOccurred(throwable);
        }
    }

    @Override
    protected File doInBackground(URL... params) {
        return download(params[0]);
    }

    private File download(final URL imageUrl) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        final File file;
        try {
            publishProgress(DOWNLOAD_STARTED_PROGRESS);
            final URLConnection connection = imageUrl.openConnection();
            connection.connect();
            final int imageSize = Integer.valueOf(connection.getHeaderField(CONTENT_LENGTH_HEADER));
            inputStream = connection.getInputStream();
            file = getTempFile();
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            performDownloadPublishingProgress(inputStream, outputStream, imageSize);
            publishProgress(DOWNLOAD_FINISHED_PROGRESS);
        } catch (final IOException e) {
            throwable = e;
            return null;
        }
        finally {
            for(final Closeable stream : new Closeable[]{inputStream, outputStream}) {
                if(stream != null) {
                    try {
                        stream.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return file;
    }

    private File getTempFile() throws IOException {
        return File.createTempFile("image", null, context.getCacheDir());
    }

    private void performDownloadPublishingProgress(final InputStream inputStream,
        final OutputStream outputStream, final int imageSize) throws IOException
    {
        final byte[] buffer = new byte[BUFFER_LENGTH];
        int totalBytesRead = 0;
        int bytesRead;
        while((bytesRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
            publishProgress(Math.round(100.f * totalBytesRead / imageSize));
        }
    }
}
