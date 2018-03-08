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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManager;
import uk.co.glass_software.android.shared_preferences.encryption.KeyStoreModule;
import uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.EncryptedSharedPreferenceStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.ENCRYPTED_STORE_NAME;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.IS_ENCRYPTION_KEY_SECURE;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.IS_ENCRYPTION_SUPPORTED;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.LENIENT_ENCRYPTED_STORE_NAME;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.STORE_NAME;

@Singleton
@Component(modules = {
        PersistenceModule.class,
        KeyStoreModule.class
})
public interface SharedPreferenceComponent {
    
    @Named(STORE_NAME)
    SharedPreferenceStore store();
    
    @Named(ENCRYPTED_STORE_NAME)
    EncryptedSharedPreferenceStore encryptedStore();

    @Named(LENIENT_ENCRYPTED_STORE_NAME)
    KeyValueStore lenientEncryptedStore();
    
    @Named(IS_ENCRYPTION_SUPPORTED)
    Boolean isEncryptionSupported();
    
    @Named(IS_ENCRYPTION_KEY_SECURE)
    Boolean isEncryptionKeySecure();
    
    @Nullable
    EncryptionManager keyStoreManager();
    
    @NonNull
    Logger logger();
}
