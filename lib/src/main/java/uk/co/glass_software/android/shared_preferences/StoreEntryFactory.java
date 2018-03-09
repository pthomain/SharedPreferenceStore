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
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import io.reactivex.Observable;
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManager;
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManagerModule;
import uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyModule;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.EncryptedSharedPreferenceStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

import static uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule.openSharedPreferences;

public class StoreEntryFactory {
    
    public final static String DEFAULT_PLAIN_TEXT_PREFERENCE_NAME = "plain_text_store";
    public final static String DEFAULT_ENCRYPTED_PREFERENCE_NAME = "encrypted_store";
    
    private final KeyValueStore store;
    private final KeyValueStore encryptedStore;
    private final KeyValueStore lenientStore;
    private final KeyValueStore forgetfulStore;
    
    @Nullable
    private final EncryptionManager encryptionManager;
    
    StoreEntryFactory(Context context,
                      @Nullable SharedPreferences plainTextPreferences,
                      @Nullable SharedPreferences encryptedPreferences,
                      boolean fallbackToCustomEncryption,
                      @Nullable Logger logger,
                      @Nullable Serialiser customSerialiser) {
        plainTextPreferences = plainTextPreferences == null ? openSharedPreferences(context, DEFAULT_PLAIN_TEXT_PREFERENCE_NAME) : plainTextPreferences;
        encryptedPreferences = encryptedPreferences == null ? openSharedPreferences(context, DEFAULT_ENCRYPTED_PREFERENCE_NAME) : encryptedPreferences;
        
        SharedPreferenceComponent component = DaggerSharedPreferenceComponent
                .builder()
                .keyModule(new KeyModule(context))
                .encryptionManagerModule(new EncryptionManagerModule(fallbackToCustomEncryption))
                .storeModule(new StoreModule(context, plainTextPreferences, encryptedPreferences, logger))
                .serialisationModule(new SerialisationModule(customSerialiser))
                .build();
        
        encryptedStore = component.encryptedStore();
        store = component.store();
        lenientStore = component.lenientStore();
        forgetfulStore = component.forgetfulStore();
        encryptionManager = component.keyStoreManager();
        
        component.logger().d(this, "Encryption supported: " + (encryptionManager != null && encryptionManager.isEncryptionSupported() ? "TRUE" : "FALSE"));
        component.logger().d(this, "Encryption key secure: " + (encryptionManager != null && encryptionManager.isEncryptionKeySecure() ? "TRUE" : "FALSE"));
    }
    
    public static StoreEntryFactoryBuilder builder(Context context) {
        return new StoreEntryFactoryBuilder(context.getApplicationContext());
    }
    
    public <C> StoreEntry<C> open(String key,
                                  Class<C> valueClass) {
        return open(() -> key, () -> valueClass);
    }
    
    public <C> StoreEntry<C> openEncrypted(String key,
                                           Class<C> valueClass) {
        return openEncrypted(() -> key, () -> valueClass);
    }
    
    public <C> StoreEntry<C> openLenient(String key,
                                         Class<C> valueClass) {
        return openLenient(() -> key, () -> valueClass);
    }
    
    public <C> StoreEntry<C> openForgetful(String key,
                                           Class<C> valueClass) {
        return openForgetful(() -> key, () -> valueClass);
    }
    
    public <C, K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider> StoreEntry<C> open(K key) {
        return open(key, key);
    }
    
    public <C, K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider> StoreEntry<C> openEncrypted(K key) {
        return openEncrypted(key, key);
    }
    
    public <C, K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider> StoreEntry<C> openLenient(K key) {
        return openEncrypted(key, key);
    }
    
    public <C, K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider> StoreEntry<C> openForgetful(K key) {
        return openForgetful(key, key);
    }
    
    private <C> StoreEntry<C> open(StoreEntry.UniqueKeyProvider keyProvider,
                                   StoreEntry.ValueClassProvider valueClassProvider) {
        return new StoreEntry<>(store, keyProvider, valueClassProvider);
    }
    
    private <C> StoreEntry<C> openEncrypted(StoreEntry.UniqueKeyProvider keyProvider,
                                            StoreEntry.ValueClassProvider valueClassProvider) {
        return new StoreEntry<>(encryptedStore, keyProvider, valueClassProvider);
    }
    
    private <C> StoreEntry<C> openLenient(StoreEntry.UniqueKeyProvider keyProvider,
                                          StoreEntry.ValueClassProvider valueClassProvider) {
        return new StoreEntry<>(lenientStore, keyProvider, valueClassProvider);
    }
    
    private <C> StoreEntry<C> openForgetful(StoreEntry.UniqueKeyProvider keyProvider,
                                            StoreEntry.ValueClassProvider valueClassProvider) {
        return new StoreEntry<>(forgetfulStore, keyProvider, valueClassProvider);
    }
    
    public KeyValueStore getStore() {
        return store;
    }
    
    public KeyValueStore getEncryptedStore() {
        return encryptedStore;
    }
    
    @Nullable
    public EncryptionManager getEncryptionManager() {
        return encryptionManager;
    }
}
