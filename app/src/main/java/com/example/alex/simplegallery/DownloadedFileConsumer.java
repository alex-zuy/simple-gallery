package com.example.alex.simplegallery;

import java.io.File;

public interface DownloadedFileConsumer {
    void consume(File file);
}
