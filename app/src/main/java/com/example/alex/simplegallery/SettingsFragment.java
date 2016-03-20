package com.example.alex.simplegallery;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.util.HashSet;
import java.util.Set;

import autovalue.shaded.com.google.common.common.collect.Lists;
import autovalue.shaded.com.google.common.common.collect.Sets;

public class SettingsFragment extends PreferenceFragment {

    private final int CHOOSE_DIRECTORY_ACTIVITY_REQUEST = 1782;

    private final int URL_LIST_ACTIVITY_REQUEST = 931843;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setUpDirectoryPreferenceHandling();
        setUpUrlListPreferenceHandling();
    }

    private void setUpUrlListPreferenceHandling() {
        final String preferenceKey = getString(R.string.pref_data_source_url_list_key);
        findPreference(preferenceKey).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Set<String> urls = getPreferenceManager().getSharedPreferences()
                        .getStringSet(preferenceKey, new HashSet<String>());
                final Intent intent = new Intent(getActivity(), UrlListActivity.class)
                        .putStringArrayListExtra(UrlListActivity.URL_LIST, Lists.newArrayList(urls));
                startActivityForResult(intent, URL_LIST_ACTIVITY_REQUEST);
                return true;
            }
        });
    }

    private void setUpDirectoryPreferenceHandling() {
        final String preferenceKey = getString(R.string.pref_data_source_directory_key);
        findPreference(preferenceKey).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Intent intent = new Intent(getActivity(), DirectoryChooserActivity.class)
                        .putExtra(DirectoryChooserActivity.EXTRA_CONFIG, getDirectoryChooserConfig());
                startActivityForResult(intent, CHOOSE_DIRECTORY_ACTIVITY_REQUEST);
                return true;
            }
        });
    }

    private DirectoryChooserConfig getDirectoryChooserConfig() {
        final String defaultDir = getString(R.string.pref_data_source_directory_default);
        final String initialDir = getPreferenceManager().getSharedPreferences()
                .getString(getString(R.string.pref_data_source_directory_key), defaultDir);
        return DirectoryChooserConfig.builder()
            .initialDirectory(initialDir)
            .allowReadOnlyDirectory(true)
            .allowNewDirectoryNameModification(false)
            .newDirectoryName(getString(R.string.directory_chooser_new_directory_name))
            .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CHOOSE_DIRECTORY_ACTIVITY_REQUEST
            && resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED)
        {
            final String directory = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
            getPreferenceManager().getSharedPreferences().edit()
                    .putString(getString(R.string.pref_data_source_directory_key), directory)
                    .commit();
        }
        else if (requestCode == URL_LIST_ACTIVITY_REQUEST
            && resultCode == UrlListActivity.URL_LIST_EDITED)
        {
            final Set<String> urls = Sets.newHashSet(data.getStringArrayListExtra(UrlListActivity.URL_LIST));
            getPreferenceManager().getSharedPreferences().edit()
                .putStringSet(getString(R.string.pref_data_source_url_list_key), urls)
                .commit();
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
