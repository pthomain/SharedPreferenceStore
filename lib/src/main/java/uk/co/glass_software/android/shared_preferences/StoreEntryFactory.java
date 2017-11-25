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
import android.support.annotation.Nullable;

import io.reactivex.Observable;
import uk.co.glass_software.android.shared_preferences.keystore.KeyStoreManager;
import uk.co.glass_software.android.shared_preferences.keystore.KeyStoreModule;
import uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule;
import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.EncryptedSharedPreferenceStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

public class StoreEntryFactory {
    
    private final SharedPreferenceStore store;
    private final EncryptedSharedPreferenceStore encryptedStore;
    private final boolean isEncryptionSupported;
    
    @Nullable
    private final KeyStoreManager keyStoreManager;
    
    public StoreEntryFactory(Context context) {
        SharedPreferenceComponent component = DaggerSharedPreferenceComponent.builder()
                                                                             .keyStoreModule(new KeyStoreModule(context.getApplicationContext()))
                                                                             .persistenceModule(new PersistenceModule(context.getApplicationContext()))
                                                                             .build();
        
        encryptedStore = component.encryptedStore();
        store = component.store();
        isEncryptionSupported = component.isEncryptionSupported();
        keyStoreManager = component.keyStoreManager();
    }
    
    public <C> StoreEntry<C> open(String key,
                                  Class<C> valueClass) {
        return open(() -> key, () -> valueClass);
    }
    
    public <C> StoreEntry<C> openEncrypted(String key,
                                           Class<C> valueClass) {
        return openEncrypted(() -> key, () -> valueClass);
    }
    
    public <C> StoreEntry<C> open(StoreEntry.UniqueKeyProvider keyProvider,
                                  StoreEntry.ValueClassProvider<C> valueClassProvider) {
        return new StoreEntry<>(store, keyProvider, valueClassProvider);
    }
    
    public <C> StoreEntry<C> openEncrypted(StoreEntry.UniqueKeyProvider keyProvider,
                                           StoreEntry.ValueClassProvider<C> valueClassProvider) {
        return new StoreEntry<>(encryptedStore, keyProvider, valueClassProvider);
    }
    
    public <C, K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider<C>> StoreEntry<C> open(K key) {
        return open(key, key);
    }
    
    public <C, K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider<C>> StoreEntry<C> openEncrypted(K key) {
        return openEncrypted(key, key);
    }
    
    public boolean isEncryptionSupported() {
        return isEncryptionSupported;
    }
    
    public Observable<String> observeChanges() {
        return store.observeChanges().mergeWith(encryptedStore.observeChanges());
    }
    
    public SharedPreferenceStore getStore() {
        return store;
    }
    
    public EncryptedSharedPreferenceStore getEncryptedStore() {
        return encryptedStore;
    }
    
    public byte[] encrypt(byte[] toEncrypt) {
        checkEncryptionSupported();
        return keyStoreManager.encryptBytes(toEncrypt);
    }
    
    public byte[] decrypt(byte[] toDecrypt) {
        checkEncryptionSupported();
        return keyStoreManager.decryptBytes(toDecrypt);
    }
    
    private void checkEncryptionSupported() {
        if (!isEncryptionSupported) {
            throw new IllegalStateException("Encryption isn't supported on this device");
        }
    }
}
