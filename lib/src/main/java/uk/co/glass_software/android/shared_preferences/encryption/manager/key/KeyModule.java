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

package uk.co.glass_software.android.shared_preferences.encryption.manager.key;

import android.content.Context;
import android.support.annotation.Nullable;

import com.facebook.crypto.CryptoConfig;

import java.security.KeyStore;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

@Module
public class KeyModule {
    
    public final static String CONFIG = "store_config";
    public static final String KEY_ALIAS_PRE_M = "KEY_ALIAS_PRE_M";
    public static final String KEY_ALIAS_POST_M = "KEY_ALIAS_POST_M";
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    
    private final String keyAlias;
    
    public KeyModule(Context context) {
        keyAlias = context.getApplicationContext().getPackageName() + "==StoreKey";
    }
    
    @Provides
    @Singleton
    @Nullable
    KeyStore provideKeyStore(Logger logger) {
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
                logger.e(this, e, "KeyStore could not be loaded");
                return null;
            }
        }
    }
    
    @Provides
    @Singleton
    @Named(KEY_ALIAS_PRE_M)
    String provideKeyAliasPreM() {
        return keyAlias + "-preM";
    }
    
    @Provides
    @Singleton
    @Named(KEY_ALIAS_POST_M)
    String provideKeyAliasPostM() {
        return keyAlias + "-postM";
    }
    
    @Provides
    @Singleton
    RsaEncrypter provideRsaEncrypter(@Nullable KeyStore keyStore,
                                     Logger logger,
                                     @Named(KEY_ALIAS_PRE_M) String keyAlias) {
        return new RsaEncrypter(
                keyStore,
                logger,
                keyAlias
        );
    }
    
    @Provides
    @Singleton
    KeyPair provideKeyPair(@Named(CONFIG) SharedPreferenceStore sharedPreferenceStore) {
        return new KeyPair(sharedPreferenceStore);
    }
    
    @Provides
    @Singleton
    CryptoConfig provideCryptoConfig() {
        return CryptoConfig.KEY_256;
    }
    
    @Provides
    @Singleton
    RsaEncryptedKeyPairProvider provideKeyPairProvider(RsaEncrypter rsaEncrypter,
                                                       Logger logger,
                                                       KeyPair keyPair,
                                                       CryptoConfig cryptoConfig) {
        return new RsaEncryptedKeyPairProvider(
                rsaEncrypter,
                logger,
                keyPair,
                cryptoConfig
        );
    }
    
}
