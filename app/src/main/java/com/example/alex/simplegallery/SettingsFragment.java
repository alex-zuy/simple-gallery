package com.example.alex.simplegallery;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.util.HashSet;
import java.util.Set;

import autovalue.shaded.com.google.common.common.collect.Lists;
import autovalue.shaded.com.google.common.common.collect.Sets;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

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
                showStorageTypeSelectDialog();
                return true;
            }
        });
    }

    private void showStorageTypeSelectDialog() {
        final Button internal = new Button(getActivity());
        internal.setText(R.string.internal_storage);
        final Button external = new Button(getActivity());
        external.setText(R.string.external_storage);
        final LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(internal, new ActionBar.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        layout.addView(external, new ActionBar.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(layout)
                .create();
        internal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDirectoryChooserActivity(getActivity().getFilesDir().getAbsolutePath());
                dialog.dismiss();
            }
        });
        external.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDirectoryChooserActivity("");
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void startDirectoryChooserActivity(final String initialDir) {
        final Intent intent = new Intent(getActivity(), DirectoryChooserActivity.class)
                .putExtra(DirectoryChooserActivity.EXTRA_CONFIG, getDirectoryChooserConfig(initialDir));
        startActivityForResult(intent, CHOOSE_DIRECTORY_ACTIVITY_REQUEST);
    }

    private DirectoryChooserConfig getDirectoryChooserConfig(final String initialDir) {
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
