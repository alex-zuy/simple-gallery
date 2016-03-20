package com.example.alex.simplegallery;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private SliderController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        controller = createSliderController();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            switchToSettingsActivity();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private SliderController createSliderController() {
        try {
            final ViewGroup root = (ViewGroup) findViewById(R.id.animation_root);
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            final DataSource dataSource = createDataSource();
            dataSource.setLoadProgressListener(new ProgressBarHandler(progressBar));
            return new SliderController(this, root, dataSource,
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

    private void switchToSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private DataSource createDataSource() {
        final String preferredDataSourceTypeKey = getString(R.string.pref_data_source_type_key);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String preferredDataSourceType = preferences.getString(preferredDataSourceTypeKey, "");
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
        final String directoryPreferenceKey = getString(R.string.pref_data_source_directory_key);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String directoryPath = preferences.getString(directoryPreferenceKey, "");
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
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
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String animation = preferences.getString(getString(R.string.pref_sliding_animation_key), "");
        try {
            return AnimationType.valueOf(animation);
        }
        catch (final IllegalArgumentException e) {
            throw new RuntimeException("Invalid animation type: " + animation);
        }
    }

    private static class ProgressBarHandler implements LoadProgressListener {

        private final ProgressBar progressBar;

        private ProgressBarHandler(final ProgressBar progressBar) {
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
