package com.example.alex.simplegallery;

public interface DataSource {

    void setConsumer(final BitmapConsumer consumer);

    void prepareNextImage();

    boolean hasNextImage();

    void destroy();
}
