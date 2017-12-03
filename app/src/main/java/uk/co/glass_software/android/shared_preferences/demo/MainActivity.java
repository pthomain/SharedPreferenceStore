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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import uk.co.glass_software.android.shared_preferences.StoreEntryFactory;
import uk.co.glass_software.android.shared_preferences.persistence.base.EncryptedStoreEntry;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.EncryptedSharedPreferenceStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.ENCRYPTED_STORE_NAME;

public class MainActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
    
    private final static SimpleDateFormat format = new SimpleDateFormat("dd/MM hh:mm:ss");
    private StoreEntryFactory storeEntryFactory;
    private SharedPreferences encryptedPreferences;
    private Disposable subscription;
    private Counter counter;
    private LastOpenDate lastOpenDate;
    
    private SharedPreferenceStore store;
    private EncryptedSharedPreferenceStore encryptedStore;
    
    @BindView(R.id.input_key)
    EditText keyEditText;
    
    @BindView(R.id.input_value)
    EditText valueEditText;
    
    @BindView(R.id.encrypted_switch)
    Switch encryptedSwitch;
    
    @BindView(R.id.entries)
    TextView entries;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        
        encryptedPreferences = getSharedPreferences(getPackageName() + "$" + ENCRYPTED_STORE_NAME,
                                                    Context.MODE_PRIVATE
        ); //used only to display encrypted values as stored on disk, should not be used directly in practice
        
        storeEntryFactory = new StoreEntryFactory(this);
        store = storeEntryFactory.getStore();
        encryptedStore = storeEntryFactory.getEncryptedStore();
        
        counter = new Counter(store);
        lastOpenDate = new LastOpenDate(encryptedStore);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        showEntries();
        subscription = storeEntryFactory.observeChanges()
                                        .subscribe(ignore -> showEntries());
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (subscription != null) {
            subscription.dispose();
        }
        counter.save(counter.get(1) + 1);
        lastOpenDate.save(format.format(new Date()));
    }
    
    @Nullable
    private StoreEntry<String> getStoreEntry() {
        String key = keyEditText.getText().toString();
        
        if (key.isEmpty()) {
            return null;
        }
        
        return encryptedSwitch.isChecked()
               ? storeEntryFactory.openEncrypted(key, String.class)
               : storeEntryFactory.open(key, String.class);
    }
    
    @OnClick(R.id.button_get)
    void onGetClicked() {
        StoreEntry<String> storeEntry = getStoreEntry();
        
        if (storeEntry != null) {
            String message = storeEntry.get("Entry doesn't exist");
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
    
    @OnClick(R.id.button_save)
    void onSaveClicked() {
        StoreEntry<String> storeEntry = getStoreEntry();
        String value = valueEditText.getText().toString();
        if (storeEntry != null) {
            storeEntry.save(value.isEmpty() ? null : value);
        }
    }
    
    @OnClick(R.id.button_delete)
    void onDeleteClicked() {
        StoreEntry<String> storeEntry = getStoreEntry();
        if (storeEntry != null) {
            storeEntry.drop();
        }
    }
    
    private void showEntries() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("\t\tApp opened ");
        builder.append(counter.get(1));
        builder.append(" time(s), last open date: ");
        builder.append(lastOpenDate.get("N/A"));
        builder.append("\n\n");
        
        showEntries(builder,
                    "Plain text entries",
                    store.getCachedValues()
        );
        
        showEntries(builder,
                    "Encrypted entries (as returned by the store)",
                    encryptedStore.getCachedValues()
        );
        
        showEntries(builder,
                    "Encrypted entries (as stored on disk)",
                    encryptedPreferences.getAll()
        );
        
        entries.setText(builder.toString());
    }
    
    private void showEntries(StringBuilder builder,
                             String title,
                             Map<String, ?> entries) {
        builder.append("* ");
        builder.append(title);
        builder.append(":\n");
        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            output(builder, entry.getKey(), entry.getValue());
        }
    }
    
    private void output(StringBuilder builder,
                        String key,
                        Object value) {
        builder.append("\n\t\t\t\t");
        builder.append(key);
        builder.append(" => ");
        builder.append(value.toString().replaceAll("\\n", ""));
    }
    
    private class Counter extends StoreEntry<Integer> {
        private Counter(@NonNull KeyValueStore store) {
            super(store, Keys.COUNTER);
        }
    }
    
    private class LastOpenDate extends EncryptedStoreEntry {
        private LastOpenDate(@NonNull EncryptedSharedPreferenceStore store) {
            super(store, Keys.LAST_OPEN_DATE);
        }
    }
    
    @OnClick(R.id.github)
    void openGithub() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse("https://github.com/pthomain/SharedPreferenceStore"));
    }
}