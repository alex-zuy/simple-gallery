package com.example.alex.simplegallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {

    private SliderController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        controller = createSliderController();
        setUpSettingsOnLongClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(controller != null && !controller.isRunning()) {
            controller.resume();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(controller != null) {
            controller.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(controller != null) {
            controller.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(controller != null) {
            controller.destroy();
        }
    }

    private void setUpSettingsOnLongClick() {
        final View view = findViewById(R.id.root);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    private SliderController createSliderController() {
        try {
            final ViewGroup animationRoot = (ViewGroup) findViewById(R.id.animation_root);
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            final DataSource dataSource = createDataSource();
            dataSource.setLoadProgressListener(new DownloadProgressHandler(progressBar));
            return new SliderController(this, animationRoot, dataSource,
                getSlidingIntervalSeconds(), getAnimationType());
        }
        catch (final DataSourceConfigurationException e) {
            showErrorInDialog(e.getMessage());
        }
        catch (final RuntimeException e) {
            showErrorInDialog("Unknown  error: " + e.getMessage());
        }
        return null;
    }

    private void showErrorInDialog(final String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error_dialog_title))
                .setMessage(message)
                .show();
    }

    private int getSlidingIntervalSeconds() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.pref_sliding_interval_key), 2);
    }

    private DataSource createDataSource() {
        final String preferredDataSourceType = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_data_source_type_key), "");
        if(preferredDataSourceType.equals(getString(R.string.data_source_type_dir))) {
            return createDirectoryDataSource();
        }
        else if(preferredDataSourceType.equals(getString(R.string.data_source_type_url_list))) {
            return createUrlListDataSource();
        }
        else {
            throw new RuntimeException("Unknown data source type: " + preferredDataSourceType);
        }
    }

    private DataSource createDirectoryDataSource() {
        final String directoryPath = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_data_source_directory_key), "");
        final File file = new File(directoryPath);
        if(!file.exists()) {
            throw new DataSourceConfigurationException(getString(R.string.err_data_source_dir_not_exists));
        }
        else if(!file.isDirectory()) {
            throw new DataSourceConfigurationException(getString(R.string.err_data_source_dir_is_not_dir));
        }
        else if(!file.canRead()) {
            throw new DataSourceConfigurationException(getString(R.string.err_data_source_dir_not_readable));
        }
        else {
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return new DirectoryDataSource(file, displayMetrics);
        }
    }

    private DataSource createUrlListDataSource() {
        final String urlListPreferenceKey = getString(R.string.pref_data_source_url_list_key);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final Set<URL> urls = new HashSet<>();
        for(final String url : preferences.getStringSet(urlListPreferenceKey, new HashSet<String>())) {
            try {
                urls.add(new URL(url));
            }
            catch (final MalformedURLException e) {
                throw new DataSourceConfigurationException(e.getLocalizedMessage(), e);
            }
        }
        return new UrlListDataSource(this, getResources().getDisplayMetrics(), urls);
    }

    private AnimationType getAnimationType() {
        final String animation = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_sliding_animation_key), "");
        try {
            return AnimationType.valueOf(animation);
        }
        catch (final IllegalArgumentException e) {
            throw new RuntimeException("Invalid animation type: " + animation);
        }
    }

    private class DownloadProgressHandler implements LoadProgressListener {

        private final ProgressBar progressBar;

        private DownloadProgressHandler(final ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        @Override
        public void progressStarted() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void progressUpdated(int percentCompleted) {
            progressBar.setProgress(percentCompleted);
        }

        @Override
        public void progressCompleted() {
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void errorOccurred(Throwable e) {
            progressBar.setVisibility(View.INVISIBLE);
            controller.pause();
            showErrorInDialog(e.getLocalizedMessage());
        }
    }

    private static class DataSourceConfigurationException extends RuntimeException {

        public DataSourceConfigurationException(final String message) {
            super(message);
        }

        public DataSourceConfigurationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
