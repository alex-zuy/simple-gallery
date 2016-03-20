package com.example.alex.simplegallery;

public interface LoadProgressListener {

    void progressStarted();

    void progressUpdated(final int percentCompleted);

    void progressCompleted();

    void errorOccurred(final Throwable e);
}
