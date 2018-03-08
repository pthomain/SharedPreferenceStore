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

import android.content.Context;
import android.support.annotation.Nullable;

import java.security.KeyStore;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyModule;
import uk.co.glass_software.android.shared_preferences.encryption.manager.key.RsaEncryptedKeyPairProvider;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyModule.KEY_ALIAS;

@Module(includes = KeyModule.class)
public class CustomModule {
    
    @Provides
    @Singleton
    PreMSecureKeyProvider providePreMSecureKeyProvider(RsaEncryptedKeyPairProvider keyPairProvider,
                                                       Context context,
                                                       @Nullable KeyStore keyStore,
                                                       Logger logger,
                                                       @Named(KEY_ALIAS) String keyAlias) {
        return new PreMSecureKeyProvider(
                keyPairProvider,
                context,
                logger,
                keyStore,
                keyAlias
        );
    }
    
    @Provides
    @Singleton
    PostMSecureKeyProvider providePostMSecureKeyProvider(@Nullable KeyStore keyStore,
                                                         Logger logger,
                                                         @Named(KEY_ALIAS) String keyAlias) {
        return new PostMSecureKeyProvider(
                keyStore,
                logger,
                keyAlias
        );
    }
    
    @Provides
    @Singleton
    @Nullable
    PreMEncryptionManager providePreMEncryptionManager(Logger logger,
                                                       PreMSecureKeyProvider secureKeyProvider) {
        if (SDK_INT >= JELLY_BEAN_MR2) {
            return new PreMEncryptionManager(
                    logger,
                    secureKeyProvider
            );
        }
        return null;
    }
    
    @Provides
    @Singleton
    @Nullable
    PostMEncryptionManager providePostMEncryptionManager(Logger logger,
                                                         PostMSecureKeyProvider secureKeyProvider) {
        if (SDK_INT >= M) {
            return new PostMEncryptionManager(
                    logger,
                    secureKeyProvider
            );
        }
        return null;
    }
}
