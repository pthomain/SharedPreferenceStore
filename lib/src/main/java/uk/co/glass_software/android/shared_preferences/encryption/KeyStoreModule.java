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

package uk.co.glass_software.android.shared_preferences.encryption;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManager;
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManagerModule;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.EncryptedSharedPreferenceStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.LenientEncryptedSharedPreferenceStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.Serialiser;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.BASE_64;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.CUSTOM;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.ENCRYPTED_STORE_NAME;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.IS_ENCRYPTION_SUPPORTED;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.LENIENT_ENCRYPTED_STORE_NAME;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.STORE_NAME;

@Module(includes = EncryptionManagerModule.class)
public class KeyStoreModule {
    
    @Provides
    @Singleton
    @Named(ENCRYPTED_STORE_NAME)
    EncryptedSharedPreferenceStore provideEncryptedSharedPreferenceStore(@Named(ENCRYPTED_STORE_NAME) SharedPreferences sharedPreferences,
                                                                         @Named(BASE_64) Serialiser base64Serialiser,
                                                                         @Nullable @Named(CUSTOM) Serialiser customSerialiser,
                                                                         @Named(ENCRYPTED_STORE_NAME) BehaviorSubject<String> changeSubject,
                                                                         Logger logger,
                                                                         @Nullable EncryptionManager encryptionManager) {
        return new EncryptedSharedPreferenceStore(
                sharedPreferences,
                base64Serialiser,
                customSerialiser,
                changeSubject,
                logger,
                encryptionManager
        );
    }
    
    
    @Provides
    @Singleton
    @Named(LENIENT_ENCRYPTED_STORE_NAME)
    KeyValueStore provideLenientEncryptedSharedPreferenceStore(@Named(STORE_NAME) SharedPreferenceStore plainTextStore,
                                                               @Named(ENCRYPTED_STORE_NAME) EncryptedSharedPreferenceStore encryptedStore,
                                                               Logger logger) {
        return new LenientEncryptedSharedPreferenceStore(
                plainTextStore,
                encryptedStore,
                logger
        );
    }
    
    @Provides
    @Singleton
    @Named(IS_ENCRYPTION_SUPPORTED)
    Boolean provideIsEncryptionSupported(@Nullable EncryptionManager encryptionManager) {
        return encryptionManager != null;
    }
   
}
