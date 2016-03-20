package com.example.alex.simplegallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.HashSet;
import java.util.Set;

import autovalue.shaded.com.google.common.common.collect.Lists;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class UrlListActivity extends AppCompatActivity {

    public static final int URL_LIST_EDITED = 1;

    public static final String URL_LIST = "URL_LIST";

    private final Set<String> urls = new HashSet<String>();

    private int urlFieldsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_list);
        urls.addAll(getIntent().getStringArrayListExtra(URL_LIST));
        urlFieldsCount = urls.size();
        setUpTable((TableLayout) findViewById(R.id.table));
    }

    private void setUpTable(final TableLayout table) {
        for (final String value : urls) {
            addRowToTable(table, value);
        }
        final int maxUrlsCount = getResources().getInteger(R.integer.pref_data_source_url_list_max_count);
        final Button addButton = (Button) findViewById(R.id.add_url);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(urlFieldsCount < maxUrlsCount) {
                    addRowToTable(table, "");
                    ++urlFieldsCount;
                }
            }
        });
    }

    private void addRowToTable(final TableLayout table, final String value) {
        final Button removeButton = new Button(this);
        removeButton.setText("-");
        final EditText editText = new EditText(this);
        editText.setText(value);
        final TableRow row = new TableRow(this);
        row.addView(removeButton, new TableRow.LayoutParams(WRAP_CONTENT, MATCH_PARENT, 1f));
        row.addView(editText, new TableRow.LayoutParams(WRAP_CONTENT, MATCH_PARENT, 4f));
        table.addView(row);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                table.removeView(row);
                urls.remove(editText.getText().toString());
                --urlFieldsCount;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                urls.remove(s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                urls.add(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void okClicked(final View okButton) {
        final Intent intent = new Intent().putStringArrayListExtra(URL_LIST, Lists.newArrayList(urls));
        setResult(URL_LIST_EDITED, intent);
        finish();
    }

    public void cancelClicked(final View cancelButton) {
        finish();
    }
}
