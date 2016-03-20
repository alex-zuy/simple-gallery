package com.example.alex.simplegallery;

public class NoOpLoadProgressListener implements LoadProgressListener {

    @Override
    public void progressStarted() { }

    @Override
    public void progressUpdated(final int percentCompleted) { }

    @Override
    public void progressCompleted() { }

    @Override
    public void errorOccurred(Throwable e) { }
}
