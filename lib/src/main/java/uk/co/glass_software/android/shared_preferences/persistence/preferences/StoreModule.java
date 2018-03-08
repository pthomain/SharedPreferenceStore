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

package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Function;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.SimpleLogger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManager;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser;

import static uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyModule.CONFIG;
import static uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule.BASE_64;
import static uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule.CUSTOM;

@Module(includes = SerialisationModule.class)
public class StoreModule {
    
    private final static int MAX_FILE_NAME_LENGTH = 127;
    public final static String PLAIN_TEXT = "plain_text";
    public final static String ENCRYPTED = "encrypted";
    public final static String LENIENT = "lenient";
    public final static String FORGETFUL = "forgetful";
    public final static String IS_ENCRYPTION_SUPPORTED = "IS_ENCRYPTION_SUPPORTED";
    public final static String IS_ENCRYPTION_KEY_SECURE = "IS_ENCRYPTION_KEY_SECURE";
    
    private final Context context;
    
    @Nullable
    private final Logger logger;
    
    public StoreModule(Context context,
                       @Nullable Logger logger) {
        this.context = context;
        this.logger = logger;
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
        int nameLength = Math.max(PLAIN_TEXT.length(), ENCRYPTED.length()) + 1;
        int availableLength = MAX_FILE_NAME_LENGTH - nameLength;
        String packageName = context.getPackageName();
        
        if (packageName.length() > availableLength) {
            packageName = packageName.substring(packageName.length() - availableLength, packageName.length());
        }
        
        return packageName + "$" + name;
    }
    
    @Provides
    @Singleton
    @Named(PLAIN_TEXT)
    SharedPreferences provideSharedPreferences(Function<String, SharedPreferences> storeFactory) {
        return storeFactory.get(PLAIN_TEXT);
    }
    
    @Provides
    @Singleton
    @Named(CONFIG)
    SharedPreferences provideConfigSharedPreferences(Function<String, SharedPreferences> storeFactory) {
        return storeFactory.get(CONFIG);
    }
    
    @Provides
    @Singleton
    @Named(ENCRYPTED)
    SharedPreferences provideEncryptedSharedPreferences(Function<String, SharedPreferences> storeFactory) {
        return storeFactory.get(ENCRYPTED);
    }
    
    @Singleton
    @Named(PLAIN_TEXT)
    BehaviorSubject<String> provideBehaviorSubject() {
        return BehaviorSubject.create();
    }
    
    @Provides
    @Singleton
    @Named(CONFIG)
    BehaviorSubject<String> provideConfigBehaviorSubject() {
        return BehaviorSubject.create();
    }
    
    @Provides
    @Singleton
    @Named(ENCRYPTED)
    BehaviorSubject<String> provideEncryptedBehaviorSubject() {
        return BehaviorSubject.create();
    }
    
    @Provides
    @Singleton
    SharedPreferenceStore provideSharedPreferenceStore(@Named(PLAIN_TEXT) SharedPreferences sharedPreferences,
                                                       @Named(BASE_64) Serialiser base64Serialiser,
                                                       @Nullable @Named(CUSTOM) Serialiser customSerialiser,
                                                       @Named(PLAIN_TEXT) BehaviorSubject<String> changeSubject,
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
    @Named(CONFIG)
    SharedPreferenceStore provideConfigSharedPreferenceStore(@Named(CONFIG) SharedPreferences sharedPreferences,
                                                             @Named(BASE_64) Serialiser base64Serialiser,
                                                             @Nullable @Named(CUSTOM) Serialiser customSerialiser,
                                                             @Named(CONFIG) BehaviorSubject<String> changeSubject,
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
    EncryptedSharedPreferenceStore provideEncryptedSharedPreferenceStore(@Named(ENCRYPTED) SharedPreferences sharedPreferences,
                                                                         @Named(BASE_64) Serialiser base64Serialiser,
                                                                         @Nullable @Named(CUSTOM) Serialiser customSerialiser,
                                                                         @Named(ENCRYPTED) BehaviorSubject<String> changeSubject,
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
    @Named(IS_ENCRYPTION_SUPPORTED)
    Boolean provideIsEncryptionSupported(@Nullable EncryptionManager encryptionManager) {
        return encryptionManager != null;
    }
    
    @Provides
    @Singleton
    @Named(PLAIN_TEXT)
    KeyValueStore provideSharedPreferenceStore(SharedPreferenceStore store) {
        return store;
    }
    
    @Provides
    @Singleton
    @Named(ENCRYPTED)
    KeyValueStore provideEncryptedSharedPreferenceStore(EncryptedSharedPreferenceStore store) {
        return store;
    }
    
    @Provides
    @Singleton
    @Named(LENIENT)
    KeyValueStore provideLenientSharedPreferenceStore(LenientEncryptedStore store) {
        return store;
    }
    
    @Provides
    @Singleton
    @Named(FORGETFUL)
    KeyValueStore provideForgetfulSharedPreferenceStore(ForgetfulEncryptedStore store) {
        return store;
    }
    
    @Provides
    @Singleton
    @NonNull
    Logger provideLogger() {
        return logger == null ? new SimpleLogger() : logger;
    }
    
}
