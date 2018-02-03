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
import android.support.annotation.Nullable;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Function;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.SimpleLogger;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.Base64Serialiser;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.Serialiser;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

@Module
public class PersistenceModule {
    
    private final static int MAX_FILE_NAME_LENGTH = 127;
    public final static String STORE_NAME = "plain_text";
    public final static String ENCRYPTED_STORE_NAME = "encrypted";
    public final static String IS_ENCRYPTION_SUPPORTED = "IS_ENCRYPTION_SUPPORTED";
    public final static String BASE_64 = "base_64";
    public final static String CUSTOM = "custom";
    
    private final Context context;
    
    @Nullable
    private final Serialiser customSerialiser;
    
    public PersistenceModule(Context context,
                             @Nullable Serialiser customSerialiser) {
        this.context = context;
        this.customSerialiser = customSerialiser;
    }
    
    @Provides
    @Singleton
    Context provideContext() {
        return context;
    }
    
    @Provides
    @Singleton
    Function<String, SharedPreferences> provideSharedPreferenceFactory() {
        return name -> context.getSharedPreferences(getStoreName(name), Context.MODE_PRIVATE);
    }
    
    private String getStoreName(String name) {
        int nameLength = Math.max(STORE_NAME.length(), ENCRYPTED_STORE_NAME.length()) + 1;
        int availableLength = MAX_FILE_NAME_LENGTH - nameLength;
        String packageName = context.getPackageName();
        
        if (packageName.length() > availableLength) {
            packageName = packageName.substring(packageName.length() - availableLength, packageName.length());
        }
        
        return packageName + "$" + name;
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
    @Named(BASE_64)
    Serialiser provideBase64Serialiser(Logger logger) {
        return new Base64Serialiser(logger);
    }
    
    @Provides
    @Singleton
    @Named(CUSTOM)
    @Nullable
    Serialiser provideCustomSerialiser() {
        return customSerialiser;
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
                                                       @Named(BASE_64) Serialiser base64Serialiser,
                                                       @Nullable @Named(CUSTOM) Serialiser customSerialiser,
                                                       @Named(STORE_NAME) BehaviorSubject<String> changeSubject,
                                                       Logger logger) {
        return new SharedPreferenceStore(
                sharedPreferences,
                base64Serialiser,
                customSerialiser,
                changeSubject,
                logger
        );
    }
    
    @Provides
    @Singleton
    Logger provideLogger() {
        return new SimpleLogger();
    }
    
}
