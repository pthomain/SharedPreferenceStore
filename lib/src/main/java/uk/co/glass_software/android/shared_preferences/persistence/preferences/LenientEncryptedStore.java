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

package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import uk.co.glass_software.android.shared_preferences.utils.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;

/**
 * This class will attempt to encrypt and save a given value
 * BUT will save in PLAIN-TEXT silently if encryption isn't supported.
 */
public class LenientEncryptedStore implements KeyValueStore {
    
    @NonNull
    private final KeyValueStore internalStore;
    
    LenientEncryptedStore(@NonNull KeyValueStore plainTextStore,
                          @NonNull KeyValueStore encryptedStore,
                          boolean isEncryptionSupported,
                          Logger logger) {
        internalStore = isEncryptionSupported ? encryptedStore : plainTextStore;
        logger.d(this,
                 "Encryption is"
                 + (isEncryptionSupported ? "" : " NOT")
                 + " supported"
        );
    }
    
    @Override
    public <V> V getValue(@NonNull String key,
                          @NonNull Class<V> valueClass,
                          V defaultValue) {
        return internalStore.getValue(key, valueClass, defaultValue);
    }
    
    @Override
    public <V> void saveValue(@NonNull String key, V value) {
        internalStore.saveValue(key, value);
    }
    
    @Override
    public boolean hasValue(@NonNull String key) {
        return internalStore.hasValue(key);
    }
    
    @Override
    public void deleteValue(@NonNull String key) {
        internalStore.deleteValue(key);
    }
    
    @Override
    public Observable<String> observeChanges() {
        return internalStore.observeChanges();
    }
}
