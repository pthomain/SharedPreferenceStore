/*
 * Copyright (C) 2017 Glass Software Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package uk.co.glass_software.android.shared_preferences.demo;

import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry;

public class MainActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
    
    private Disposable subscription;
    private ExpandableListAdapter listAdapter;
    private MainPresenter presenter;
    
    @BindView(R.id.input_key)
    EditText keyEditText;
    
    @BindView(R.id.input_value)
    EditText valueEditText;
    
    @BindView(R.id.encrypted_switch)
    Switch encryptedSwitch;
    
    @BindView(R.id.result)
    ExpandableListView listView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        
        presenter = new MainPresenter(this);
        listAdapter = new ExpandableListAdapter(this, presenter);
        listView.setAdapter(listAdapter);
        
        listAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                for (int i = 0; i < listAdapter.getGroupCount(); i++) {
                    listView.expandGroup(i);
                }
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        listAdapter.showEntries();
        subscription = presenter.observeChanges()
                                .subscribe(ignore -> listAdapter.showEntries());
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (subscription != null) {
            subscription.dispose();
        }
        presenter.onPause();
    }
    
    @OnClick(R.id.button_save)
    void onSaveClicked() {
        KeyValueEntry<String> storeEntry = presenter.getStoreEntry(keyEditText, encryptedSwitch);
        String value = valueEditText.getText().toString();
        storeEntry.save(value.isEmpty() ? null : value);
    }
    
    @OnClick(R.id.button_delete)
    void onDeleteClicked() {
        KeyValueEntry<String> storeEntry = presenter.getStoreEntry(keyEditText, encryptedSwitch);
        storeEntry.drop();
    }
    
    @OnClick(R.id.github)
    void openGithub() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse("https://github.com/pthomain/SharedPreferenceStore"));
    }
}