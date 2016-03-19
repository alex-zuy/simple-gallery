package com.example.alex.simplegallery;

public interface DataSource {
    void prepareNextImage(final BitmapConsumer consumer);

    boolean hasNextImage();

    void destroy();
}
