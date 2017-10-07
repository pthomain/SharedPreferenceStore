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

package uk.co.glass_software.android.shared_preferences.persistence.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import uk.co.glass_software.android.shared_preferences.keystore.KeyStoreManager;


/**
 * Values saved here will only be persisted to disk if the KeyStoreManager is not null.
 * If it is, the value will only be held in memory for the the duration of the application context.
 */
public class EncryptedStoreEntry<K extends StoreEntry.UniqueKeyProvider> extends StoreEntry<K, String> {
    
    @Nullable
    private final KeyStoreManager keyStoreManager;
    
    @Nullable
    private String memoryValue;
    
    protected EncryptedStoreEntry(@NonNull KeyValueStore store,
                                  @Nullable KeyStoreManager keyStoreManager,
                                  @NonNull K storeKey) {
        super(store, storeKey, String.class);
        this.keyStoreManager = keyStoreManager;
    }
    
    @Override
    public synchronized final void save(@Nullable String value) {
        super.save(encrypt(value));
        memoryValue = value;
    }
    
    @Override
    @Nullable
    public synchronized final String get() {
        return get(null);
    }
    
    @Override
    @Nullable
    public synchronized final String get(@Nullable String defaultValue) {
        if (memoryValue == null) {
            memoryValue = decrypt(super.get(defaultValue));
        }
        return memoryValue;
    }
    
    @Override
    public synchronized final void drop() {
        super.drop();
        memoryValue = null;
    }
    
    @Nullable
    private String encrypt(@Nullable String clearText) {
        return keyStoreManager == null || clearText == null
               ? null
               : keyStoreManager.encrypt(clearText);
    }
    
    @Nullable
    private String decrypt(@Nullable String encrypted) {
        return keyStoreManager == null || encrypted == null
               ? null
               : keyStoreManager.decrypt(encrypted);
    }
}
