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

package uk.co.glass_software.android.shared_preferences.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Function;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.SimpleLogger;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.Base64Serialiser;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

@Module
public class PersistenceModule {
    
    public final static String STORE_NAME = "shared_preference_store";
    public final static String ENCRYPTED_STORE_NAME = "shared_preference_store_encrypted";
    
    private final Context context;
    
    public PersistenceModule(Context context) {
        this.context = context;
    }
    
    @Provides
    @Singleton
    Context provideContext() {
        return context;
    }
    
    @Provides
    @Singleton
    Function<String, SharedPreferences> provideSharedPreferenceFactory() {
        return name -> context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }
    
    @Provides
    @Singleton
    @Named(STORE_NAME)
    SharedPreferences provideSharedPreferences(Function<String, SharedPreferences> storeFactory) {
        return storeFactory.get(STORE_NAME);
    }
    
    @Provides
    @Singleton
    @Named(ENCRYPTED_STORE_NAME)
    SharedPreferences provideEncryptedSharedPreferences(Function<String, SharedPreferences> storeFactory) {
        return storeFactory.get(ENCRYPTED_STORE_NAME);
    }
    
    @Provides
    @Singleton
    Base64Serialiser provideBase64Serialiser(Logger logger) {
        return new Base64Serialiser(logger);
    }
    
    @Provides
    @Singleton
    @Named(STORE_NAME)
    BehaviorSubject<String> provideBehaviorSubject() {
        return BehaviorSubject.create();
    }
    
    @Provides
    @Singleton
    @Named(ENCRYPTED_STORE_NAME)
    BehaviorSubject<String> provideEncryptedBehaviorSubject() {
        return BehaviorSubject.create();
    }
    
    @Provides
    @Singleton
    @Named(STORE_NAME)
    SharedPreferenceStore provideSharedPreferenceStore(@Named(STORE_NAME) SharedPreferences sharedPreferences,
                                                       Base64Serialiser base64Serialiser,
                                                       @Named(STORE_NAME) BehaviorSubject<String> changeSubject,
                                                       Logger log) {
        return new SharedPreferenceStore(sharedPreferences, base64Serialiser, changeSubject, log);
    }
    
    @Provides
    @Singleton
    @Named(ENCRYPTED_STORE_NAME)
    SharedPreferenceStore provideEncryptedSharedPreferenceStore(@Named(ENCRYPTED_STORE_NAME) SharedPreferences sharedPreferences,
                                                                Base64Serialiser base64Serialiser,
                                                                @Named(ENCRYPTED_STORE_NAME) BehaviorSubject<String> changeSubject,
                                                                Logger log) {
        return new SharedPreferenceStore(sharedPreferences, base64Serialiser, changeSubject, log);
    }
    
    @Provides
    @Singleton
    Logger provideLogger() {
        return new SimpleLogger(context);
    }
    
}
