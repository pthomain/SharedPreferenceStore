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

package uk.co.glass_software.android.shared_preferences.keystore;

import android.support.annotation.Nullable;
import android.util.Base64;

import java.security.SecureRandom;

import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry;

public class SavedEncryptedAesKey extends StoreEntry<StoreEntry.UniqueKeyProvider, String> {
    
    private static final String KEY = "SharedPreferenceStoreEncryptedAesKey";
    private final Logger logger;
    private final RsaEncrypter rsaEncrypter;
    
    public SavedEncryptedAesKey(KeyValueStore store,
                                Logger logger,
                                RsaEncrypter rsaEncrypter) {
        super(store, () -> KEY, String.class, null);
        this.logger = logger;
        this.rsaEncrypter = rsaEncrypter;
    }
    
    @Override
    @Nullable
    public synchronized String get() {
        return get(null);
    }
    
    @Override
    @Nullable
    public synchronized String get(String defaultValue) {
        String storedKey = super.get(defaultValue);
        
        if (storedKey == null) {
            try {
                storedKey = generateNewKey();
                super.save(storedKey);
            }
            catch (Exception e) {
                logger.e(this, e, "Could not generate a new key");
                return null;
            }
        }
        
        return storedKey;
    }
    
    @Nullable
    byte[] getBytes() throws Exception {
        String storedKey = get();
        
        if (storedKey == null) {
            return null;
        }
        
        byte[] encryptedKey = Base64.decode(storedKey, Base64.DEFAULT);
        return rsaEncrypter.decrypt(encryptedKey);
    }
    
    private String generateNewKey() throws Exception {
        byte[] key = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);
        
        byte[] encryptedKey = rsaEncrypter.encrypt(key);
        return Base64.encodeToString(encryptedKey, Base64.DEFAULT);
    }
    
    @Override
    public synchronized final void save(String value) {
        throw new IllegalStateException("This value should not be saved outside this class");
    }
}
