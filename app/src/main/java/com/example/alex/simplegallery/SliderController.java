package com.example.alex.simplegallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class SliderController {

    private final Context context;

    private final DataSource dataSource;

    private final int slidingIntervalMilliseconds;

    private final ViewGroup root;

    private final AnimationType animationType;

    private final ImageView evenImage;

    private final ImageView oddImage;

    private ImageView currentImageView;

    private long nextSwitchTimestamp;

    private boolean isRunning;

    public SliderController(final Context context, final ViewGroup slideshowRoot,
        final DataSource dataSource, final int slidingIntervalSeconds, final AnimationType animationType)
    {
        this.context = context;
        this.animationType = animationType;
        this.dataSource = dataSource;
        this.slidingIntervalMilliseconds = slidingIntervalSeconds * 1000;
        root = slideshowRoot;
        evenImage = new ImageView(context);
        oddImage = new ImageView(context);
        currentImageView = oddImage;
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
                if (isRunning) {
                    final long leftToSwitchByInterval = nextSwitchTimestamp - System.currentTimeMillis();
                    final long delay = leftToSwitchByInterval > 0 ? leftToSwitchByInterval : 0;
                    nextSwitchTimestamp += slidingIntervalMilliseconds;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            performTransition(bitmap);
                            if (dataSource.hasNextImage()) {
                                next();
                            }
                        }
                    }, delay);
                }
            }
        });
    }

    private void performTransition(final Bitmap bitmap) {
        final int duration = 1000;
        final ImageView appearing = evenImage != currentImageView ? evenImage : oddImage;
        final ImageView disappearing = oddImage == currentImageView ? oddImage : evenImage;
        currentImageView = appearing;
        currentImageView.setImageBitmap(bitmap);
        TransitionManager.beginDelayedTransition(root,
                getTransition(appearing, disappearing, duration));
        root.addView(appearing);
        root.removeView(disappearing);
    }

    private Transition getTransition(final View appearing, final View disappearing, final int duration) {
        switch (animationType) {
            case NONE:return getIdentityTransition();
            case FADE: return getFadeTransition(appearing, disappearing, duration);
            default: throw new RuntimeException("Unknown animation type: " + animationType);
        }
    }

    private Transition getFadeTransition(final View appearing, final View disappearing, final int duration) {
        final Fade in = new Fade(Fade.IN);
        in.addTarget(appearing);
        in.setDuration(duration);
        final Fade out = new Fade(Fade.OUT);
        out.addTarget(disappearing);
        out.setDuration(duration);
        final TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(in);
        transitionSet.addTransition(out);
        return transitionSet;
    }

    private Transition getIdentityTransition() {
        return new TransitionSet();
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
