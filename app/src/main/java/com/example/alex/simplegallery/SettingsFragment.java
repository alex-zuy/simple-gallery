package com.example.alex.simplegallery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

public class SettingsFragment extends PreferenceFragment {

    private final int CHOOSE_DIRECTORY_ACTIVITY_REQUEST = 1782;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setUpDirectoryPreferenceHandling();
    }

    private void setUpDirectoryPreferenceHandling() {
        final String preferenceKey = getResources().getString(R.string.pref_data_source_directory_key);
        findPreference(preferenceKey).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Intent intent = new Intent();
                intent.setClass(getActivity(), DirectoryChooserActivity.class);
                intent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, getDirectoryChooserConfig());
                startActivityForResult(intent, CHOOSE_DIRECTORY_ACTIVITY_REQUEST);
                return true;
            }
        });
    }

    private DirectoryChooserConfig getDirectoryChooserConfig() {
        final String preferenceKey = getResources().getString(R.string.pref_data_source_directory_key);
        final String defaultDir = getResources().getString(R.string.pref_data_source_directory_default);
        final String initialDir = getPreferenceManager().getSharedPreferences().getString(preferenceKey, defaultDir);
        return DirectoryChooserConfig.builder()
            .initialDirectory(initialDir)
            .allowReadOnlyDirectory(true)
            .allowNewDirectoryNameModification(false)
            .newDirectoryName(getResources().getString(R.string.directory_chooser_new_directory_name))
            .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CHOOSE_DIRECTORY_ACTIVITY_REQUEST
            && resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED)
        {
            final String directory = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
            final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
            final Editor editor = preferences.edit();
            editor.putString(getResources().getString(R.string.pref_data_source_directory_key), directory);
            editor.commit();
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
