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

package uk.co.glass_software.android.shared_preferences;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManager;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry;
import uk.co.glass_software.android.shared_preferences.utils.Logger;
import uk.co.glass_software.android.shared_preferences.utils.StoreKey;
import uk.co.glass_software.android.shared_preferences.utils.StoreMode;

public class StoreEntryFactory {
    
    public final static String DEFAULT_PLAIN_TEXT_PREFERENCE_NAME = "plain_text_store";
    public final static String DEFAULT_ENCRYPTED_PREFERENCE_NAME = "encrypted_store";
    
    @NonNull
    private final KeyValueStore plainTextStore;
    
    @NonNull
    private final KeyValueStore encryptedStore;
    
    @NonNull
    private final KeyValueStore lenientStore;
    
    @NonNull
    private final KeyValueStore forgetfulStore;
    
    @Nullable
    private final EncryptionManager encryptionManager;
    
    StoreEntryFactory(@NonNull Logger logger,
                      @NonNull KeyValueStore store,
                      @NonNull KeyValueStore encryptedStore,
                      @NonNull KeyValueStore lenientStore,
                      @NonNull KeyValueStore forgetfulStore,
                      @Nullable EncryptionManager encryptionManager) {
        this.plainTextStore = store;
        this.encryptedStore = encryptedStore;
        this.lenientStore = lenientStore;
        this.forgetfulStore = forgetfulStore;
        this.encryptionManager = encryptionManager;
        
        logger.d(this, "Encryption supported: " + (this.encryptionManager != null && this.encryptionManager.isEncryptionSupported() ? "TRUE" : "FALSE"));
    }
    
    public static StoreEntryFactory buildDefault(Context context) {
        return builder(context).build();
    }
    
    public static StoreEntryFactoryBuilder builder(Context context) {
        return new StoreEntryFactoryBuilder(context.getApplicationContext());
    }
    
    @NonNull
    public <C> KeyValueEntry<C> open(StoreKey key) {
        return open(
                key.getUniqueKey(),
                key.getMode(),
                key.getValueClass()
        );
    }
    
    @NonNull
    public <C> KeyValueEntry<C> open(String key,
                                     StoreMode mode,
                                     Class<C> valueClass) {
        KeyValueStore store;
        
        switch (mode) {
            case PLAIN_TEXT:
                store = plainTextStore;
                break;
            
            case ENCRYPTED:
                store = encryptedStore;
                break;
            
            case LENIENT:
                store = lenientStore;
                break;
            
            case FORGETFUL:
                store = forgetfulStore;
                break;
            
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
        
        return open(store, () -> key, () -> valueClass);
    }
    
    @NonNull
    private <C> KeyValueEntry<C> open(KeyValueStore store,
                                      StoreEntry.UniqueKeyProvider keyProvider,
                                      StoreEntry.ValueClassProvider valueClassProvider) {
        return new StoreEntry<>(store, keyProvider, valueClassProvider);
    }
    
    @NonNull
    public KeyValueStore getPlainTextStore() {
        return plainTextStore;
    }
    
    @NonNull
    public KeyValueStore getEncryptedStore() {
        return encryptedStore;
    }
    
    @NonNull
    public KeyValueStore getLenientStore() {
        return lenientStore;
    }
    
    @NonNull
    public KeyValueStore getForgetfulStore() {
        return forgetfulStore;
    }
    
    @Nullable
    public EncryptionManager getEncryptionManager() {
        return encryptionManager;
    }
}
