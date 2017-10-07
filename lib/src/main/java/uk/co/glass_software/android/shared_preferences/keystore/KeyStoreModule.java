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

import android.content.Context;
import android.support.annotation.Nullable;

import java.security.KeyStore;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;

@Module
public class KeyStoreModule {
    
    private final static String APP_KEY_ALIAS = "uk.co.glass_software.shared_preferences";
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    public final static String ENCRYPTED_STORE_NAME = "encrypted";
    
    @Provides
    @Singleton
    @Nullable
    KeyStore provideKeyStore() {
        if (SDK_INT < JELLY_BEAN_MR2) {
            return null;
        }
        else {
            try {
                KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
                keyStore.load(null);
                return keyStore;
            }
            catch (Exception e) {
                return null;
            }
        }
    }
    
    @Provides
    @Singleton
    @Nullable
    SavedEncryptedAesKey provideSavedEncryptedAesKey(@Named(ENCRYPTED_STORE_NAME) SharedPreferenceStore sharedPreferenceStore,
                                                     @Nullable RsaEncrypter rsaEncrypter,
                                                     Logger logger) {
        if (SDK_INT < JELLY_BEAN_MR2 || SDK_INT >= M) {
            return null;
        }
        else {
            return new SavedEncryptedAesKey(
                    sharedPreferenceStore,
                    logger,
                    rsaEncrypter
            );
        }
    }
    
    @Provides
    @Singleton
    @Nullable
    RsaEncrypter provideRsaEncrypter(@Nullable KeyStore keyStore) {
        return new RsaEncrypter(keyStore, APP_KEY_ALIAS);
    }
    
    @Provides
    @Singleton
    @Nullable
    KeyStoreManager provideKeyStoreManager(Logger logger,
                                           @Nullable SavedEncryptedAesKey encryptedAesKey,
                                           @Nullable KeyStore keyStore,
                                           Context applicationContext) {
        if (SDK_INT < JELLY_BEAN_MR2 || keyStore == null) {
            return null;
        }
        else if (SDK_INT < M) {
            return new PreMKeyStoreManager(logger,
                                           keyStore,
                                           encryptedAesKey,
                                           APP_KEY_ALIAS,
                                           applicationContext
            );
        }
        else {
            return new PostMKeyStoreManager(logger,
                                            keyStore,
                                            APP_KEY_ALIAS
            );
        }
    }
    
}
