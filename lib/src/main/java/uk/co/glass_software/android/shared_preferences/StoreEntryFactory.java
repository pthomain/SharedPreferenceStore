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
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry;
import uk.co.glass_software.android.shared_preferences.utils.Logger;

public class StoreEntryFactory {
    
    public final static String DEFAULT_PLAIN_TEXT_PREFERENCE_NAME = "plain_text_store";
    public final static String DEFAULT_ENCRYPTED_PREFERENCE_NAME = "encrypted_store";
    
    @NonNull
    private final KeyValueStore store;
    
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
        this.store = store;
        this.encryptedStore = encryptedStore;
        this.lenientStore = lenientStore;
        this.forgetfulStore = forgetfulStore;
        this.encryptionManager = encryptionManager;
        
        logger.d(this, "Encryption supported: " + (this.encryptionManager != null && this.encryptionManager.isEncryptionSupported() ? "TRUE" : "FALSE"));
    }
    
    public static StoreEntryFactoryBuilder builder(Context context) {
        return new StoreEntryFactoryBuilder(context.getApplicationContext());
    }
    
    @NonNull
    public <C> StoreEntry<C> open(String key,
                                  Class<C> valueClass) {
        return open(() -> key, () -> valueClass);
    }
    
    @NonNull
    public <C> StoreEntry<C> openEncrypted(String key,
                                           Class<C> valueClass) {
        return openEncrypted(() -> key, () -> valueClass);
    }
    
    @NonNull
    public <C> StoreEntry<C> openLenient(String key,
                                         Class<C> valueClass) {
        return openLenient(() -> key, () -> valueClass);
    }
    
    @NonNull
    public <C> StoreEntry<C> openForgetful(String key,
                                           Class<C> valueClass) {
        return openForgetful(() -> key, () -> valueClass);
    }
    
    @NonNull
    public <C, K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider> StoreEntry<C> open(K key) {
        return open(key, key);
    }
    
    @NonNull
    public <C, K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider> StoreEntry<C> openEncrypted(K key) {
        return openEncrypted(key, key);
    }
    
    @NonNull
    public <C, K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider> StoreEntry<C> openLenient(K key) {
        return openEncrypted(key, key);
    }
    
    @NonNull
    public <C, K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider> StoreEntry<C> openForgetful(K key) {
        return openForgetful(key, key);
    }
    
    @NonNull
    private <C> StoreEntry<C> open(StoreEntry.UniqueKeyProvider keyProvider,
                                   StoreEntry.ValueClassProvider valueClassProvider) {
        return new StoreEntry<>(store, keyProvider, valueClassProvider);
    }
    
    @NonNull
    private <C> StoreEntry<C> openEncrypted(StoreEntry.UniqueKeyProvider keyProvider,
                                            StoreEntry.ValueClassProvider valueClassProvider) {
        return new StoreEntry<>(encryptedStore, keyProvider, valueClassProvider);
    }
    
    @NonNull
    private <C> StoreEntry<C> openLenient(StoreEntry.UniqueKeyProvider keyProvider,
                                          StoreEntry.ValueClassProvider valueClassProvider) {
        return new StoreEntry<>(lenientStore, keyProvider, valueClassProvider);
    }
    
    @NonNull
    private <C> StoreEntry<C> openForgetful(StoreEntry.UniqueKeyProvider keyProvider,
                                            StoreEntry.ValueClassProvider valueClassProvider) {
        return new StoreEntry<>(forgetfulStore, keyProvider, valueClassProvider);
    }
    
    @NonNull
    public KeyValueStore getStore() {
        return store;
    }
    
    @NonNull
    public KeyValueStore getEncryptedStore() {
        return encryptedStore;
    }
    
    @Nullable
    public EncryptionManager getEncryptionManager() {
        return encryptionManager;
    }
}
