package com.example.alex.simplegallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

public class SliderController {

    private final Context context;

    private final ImageView imageView;

    private final DataSource dataSource;

    private final int slidingIntervalMilliseconds;

    private long nextSwitchTimestamp;

    private boolean isRunning;

    public SliderController(final Context context, final ImageView imageView,
        final DataSource dataSource, final int slidingIntervalSeconds)
    {
        this.context = context;
        this.imageView = imageView;
        this.dataSource = dataSource;
        this.slidingIntervalMilliseconds = slidingIntervalSeconds * 1000;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void start() {
        isRunning = true;
        //show first image as soon, as it will be loaded
        nextSwitchTimestamp = System.currentTimeMillis();
        if(dataSource.hasNextImage()) {
            next();
        }
    }

    public void destroy() {
        dataSource.destroy();
    }

    private void next() {
        dataSource.prepareNextImage(new BitmapConsumer() {
            @Override
            public void consume(final Bitmap bitmap) {
                if(isRunning) {
                    final long leftToSwitchByInterval = nextSwitchTimestamp - System.currentTimeMillis();
                    final long delay = leftToSwitchByInterval > 0 ? leftToSwitchByInterval : 0;
                    nextSwitchTimestamp += slidingIntervalMilliseconds;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                            if(dataSource.hasNextImage()) {
                                next();
                            }
                        }
                    }, delay);
                }
            }
        });
    }

    public void pause() {
        isRunning = false;
    }

    public void resume() {
        isRunning = true;
        nextSwitchTimestamp = System.currentTimeMillis();
        if(dataSource.hasNextImage()) {
            next();
        }
    }
}
