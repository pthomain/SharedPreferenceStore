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

package shared_preferences.android.glass_software.co.uk.shared_preference_store_demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import uk.co.glass_software.android.shared_preferences.StoreEntryFactory;
import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry;
import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry.UniqueKeyProvider;

public class MainActivity extends AppCompatActivity {
    
    private StoreEntryFactory storeEntryFactory;
    private Disposable subscription;
    
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
        storeEntryFactory = new StoreEntryFactory(this);
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
    }
    
    @Nullable
    private StoreEntry<UniqueKeyProvider, String> getStoreEntry() {
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
        StoreEntry<UniqueKeyProvider, String> storeEntry = getStoreEntry();
        
        if (storeEntry != null) {
            String message = storeEntry.get("Entry doesn't exist");
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
    
    @OnClick(R.id.button_save)
    void onSaveClicked() {
        StoreEntry<UniqueKeyProvider, String> storeEntry = getStoreEntry();
        String value = valueEditText.getText().toString();
        if (storeEntry != null) {
            storeEntry.save(value.isEmpty() ? null : value);
        }
    }
    
    @OnClick(R.id.button_delete)
    void onDeleteClicked() {
        StoreEntry<UniqueKeyProvider, String> storeEntry = getStoreEntry();
        if (storeEntry != null) {
            storeEntry.drop();
        }
    }
    
    private void showEntries() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("\t\t* Plain text entries:\n");
        for (Map.Entry<String, Object> entry : storeEntryFactory.getStore().getCachedValues().entrySet()) {
            output(builder, entry);
        }
        
        builder.append("\n\n\t\t* Encrypted entries:\n");
        for (Map.Entry<String, Object> entry : storeEntryFactory.getEncryptedStore().getCachedValues().entrySet()) {
            output(builder, entry);
        }
    
        entries.setText(builder.toString());
    }
    
    private void output(StringBuilder builder,
                        Map.Entry<String, Object> entry) {
        builder.append("\n");
        builder.append(entry.getKey());
        builder.append(" => ");
        builder.append(entry.getValue());
    }
}
