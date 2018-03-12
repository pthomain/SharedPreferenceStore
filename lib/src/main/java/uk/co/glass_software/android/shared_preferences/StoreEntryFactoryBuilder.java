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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser;
import uk.co.glass_software.android.shared_preferences.utils.Logger;

import static uk.co.glass_software.android.shared_preferences.StoreEntryFactory.DEFAULT_ENCRYPTED_PREFERENCE_NAME;
import static uk.co.glass_software.android.shared_preferences.StoreEntryFactory.DEFAULT_PLAIN_TEXT_PREFERENCE_NAME;
import static uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule.openSharedPreferences;

public class StoreEntryFactoryBuilder {
    
    private final Context context;
    private SharedPreferences plainTextPreferences;
    private SharedPreferences encryptedPreferences;
    private Logger logger;
    private Serialiser customSerialiser;
    
    StoreEntryFactoryBuilder(Context context) {
        this.context = context;
    }
    
    @NonNull
    public StoreEntryFactoryBuilder plainTextPreferences(SharedPreferences preferences) {
        this.plainTextPreferences = preferences;
        return this;
    }
    
    @NonNull
    public StoreEntryFactoryBuilder encryptedPreferences(SharedPreferences preferences) {
        this.encryptedPreferences = preferences;
        return this;
    }
    
    @NonNull
    public StoreEntryFactoryBuilder logger(Logger logger) {
        this.logger = logger;
        return this;
    }
    
    @NonNull
    public StoreEntryFactoryBuilder customSerialiser(Serialiser customSerialiser) {
        this.customSerialiser = customSerialiser;
        return this;
    }
    
    @NonNull
    public StoreEntryFactory build() {
        plainTextPreferences = plainTextPreferences == null ? openSharedPreferences(context, DEFAULT_PLAIN_TEXT_PREFERENCE_NAME) : plainTextPreferences;
        encryptedPreferences = encryptedPreferences == null ? openSharedPreferences(context, DEFAULT_ENCRYPTED_PREFERENCE_NAME) : encryptedPreferences;
        
        SharedPreferenceComponent component = DaggerSharedPreferenceComponent
                .builder()
                .storeModule(new StoreModule(context, plainTextPreferences, encryptedPreferences, logger))
                .serialisationModule(new SerialisationModule(customSerialiser))
                .build();
        
        return new StoreEntryFactory(
                component.logger(),
                component.store(),
                component.encryptedStore(),
                component.lenientStore(),
                component.forgetfulStore(),
                component.keyStoreManager()
        );
    }
}
