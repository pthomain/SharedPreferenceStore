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

package uk.co.glass_software.android.shared_preferences.encryption.manager.custom;

import android.annotation.TargetApi;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;

import java.security.Key;
import java.security.KeyStore;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.KeyGenerator;

import uk.co.glass_software.android.shared_preferences.Logger;

import static android.os.Build.VERSION_CODES.M;
import static android.security.keystore.KeyProperties.KEY_ALGORITHM_AES;
import static android.security.keystore.KeyProperties.PURPOSE_DECRYPT;
import static android.security.keystore.KeyProperties.PURPOSE_ENCRYPT;
import static uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyModule.ANDROID_KEY_STORE;

public class PostMSecureKeyProvider implements SecureKeyProvider {
    
    @Nullable
    private final KeyStore keyStore;
    
    private final Logger logger;
    private final String keyAlias;
    
    PostMSecureKeyProvider(@Nullable KeyStore keyStore,
                           Logger logger,
                           String keyAlias) {
        this.keyStore = keyStore;
        this.logger = logger;
        this.keyAlias = keyAlias;
        createNewKeyPairIfNeeded();
    }
    
    @Override
    @Nullable
    public Key getKey() throws Exception {
        return keyStore == null ? null : keyStore.getKey(keyAlias, null);
    }
    
    @Override
    @TargetApi(M)
    public synchronized void createNewKeyPairIfNeeded() {
        try {
            if (keyStore != null && !keyStore.containsAlias(keyAlias)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KEY_ALGORITHM_AES,
                        ANDROID_KEY_STORE
                );
                
                AlgorithmParameterSpec spec = new KeyGenParameterSpec.Builder(
                        keyAlias,
                        PURPOSE_ENCRYPT | PURPOSE_DECRYPT
                )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .build();
                
                keyGenerator.init(spec);
                keyGenerator.generateKey();
            }
            
            if (!keyStore.containsAlias(keyAlias)) {
                throw new IllegalStateException("Key pair was not generated");
            }
        }
        catch (Exception e) {
            logger.e(this, e, "Could not create a new key");
        }
    }
    
    @Override
    public boolean isEncryptionSupported() {
        return keyStore != null;
    }
    
    @Override
    public boolean isEncryptionKeySecure() {
        return isEncryptionSupported();
    }
}
