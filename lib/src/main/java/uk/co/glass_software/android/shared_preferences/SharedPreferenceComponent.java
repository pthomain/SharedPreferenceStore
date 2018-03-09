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
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManagerModule;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule;

import static uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule.ENCRYPTED;
import static uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule.FORGETFUL;
import static uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule.LENIENT;
import static uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule.PLAIN_TEXT;


@Singleton
@Component(modules = {
        EncryptionManagerModule.class,
        StoreModule.class
})
public interface SharedPreferenceComponent {
    
    @Named(PLAIN_TEXT)
    KeyValueStore store();
    
    @Named(ENCRYPTED)
    KeyValueStore encryptedStore();
    
    @Named(LENIENT)
    KeyValueStore lenientStore();
    
    @Named(FORGETFUL)
    KeyValueStore forgetfulStore();
    
    @Nullable
    EncryptionManager keyStoreManager();
    
    @NonNull
    Logger logger();
}
