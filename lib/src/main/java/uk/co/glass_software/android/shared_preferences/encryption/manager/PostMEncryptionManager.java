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

package uk.co.glass_software.android.shared_preferences.encryption.manager;

import android.annotation.TargetApi;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.security.Key;
import java.security.KeyStore;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.key.SecureKeyProvider;

import static android.os.Build.VERSION_CODES.M;
import static android.security.keystore.KeyProperties.KEY_ALGORITHM_AES;
import static android.security.keystore.KeyProperties.PURPOSE_DECRYPT;
import static android.security.keystore.KeyProperties.PURPOSE_ENCRYPT;
import static uk.co.glass_software.android.shared_preferences.encryption.key.KeyModule.ANDROID_KEY_STORE;

public class PostMEncryptionManager extends BaseCustomEncryptionManager {
    
    //see https://medium.com/@ericfu/securely-storing-secrets-in-an-android-application-501f030ae5a3#.qcgaaeaso
    private final static String FIXED_IV = "ABkbm8HC1ytJ";
    private static final String AES_MODE = "AES/CBC/NoPadding";
    
    private final Logger logger;
    private final SecureKeyProvider secureKeyProvider;
    private final KeyStore keyStore;
    private final String alias;
    
    PostMEncryptionManager(Logger logger,
                           SecureKeyProvider secureKeyProvider,
                           @Nullable KeyStore keyStore,
                           String alias) {
        super(logger);
        this.logger = logger;
        this.secureKeyProvider = secureKeyProvider;
        this.keyStore = keyStore;
        this.alias = alias;
    }
    
    @NonNull
    @TargetApi(M)
    protected Cipher getCipher(boolean isEncrypt) throws Exception {
        Key secretKey = secureKeyProvider.getKey();
        
        if (secretKey == null) {
            throw new IllegalStateException("Could not retrieve the secret key");
        }
        else {
            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
                        secretKey,
                        new GCMParameterSpec(128, FIXED_IV.getBytes())
            );
            return cipher;
        }
    }
    
    
    @Override
    @TargetApi(M)
    protected synchronized void createNewKeyPairIfNeeded() {
        try {
            if (keyStore != null && !keyStore.containsAlias(alias)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KEY_ALGORITHM_AES,
                        ANDROID_KEY_STORE
                );
                
                AlgorithmParameterSpec spec = new KeyGenParameterSpec.Builder(
                        alias,
                        PURPOSE_ENCRYPT | PURPOSE_DECRYPT
                )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .build();
                
                keyGenerator.init(spec);
                keyGenerator.generateKey();
            }
        }
        catch (Exception e) {
            logger.e(this, e, "Could not create a new key");
        }
    }
}
