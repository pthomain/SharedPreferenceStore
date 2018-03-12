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
import uk.co.glass_software.android.shared_preferences.utils.Function;
import uk.co.glass_software.android.shared_preferences.utils.Logger;
import uk.co.glass_software.android.shared_preferences.utils.SimpleLogger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManager;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser;

import static uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule.BASE_64;
import static uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule.CUSTOM;

@Module(includes = SerialisationModule.class)
public class StoreModule {
    
    private final static int MAX_FILE_NAME_LENGTH = 127;
    public final static String PLAIN_TEXT = "plain_text";
    public final static String ENCRYPTED = "encrypted";
    public final static String LENIENT = "lenient";
    public final static String FORGETFUL = "forgetful";
    
    private final Context context;
    private final SharedPreferences plainTextSharedPreferences;
    private final SharedPreferences encryptedSharedPreferences;
    
    @Nullable
    private final Logger logger;
    
    public StoreModule(@NonNull Context context,
                       @NonNull SharedPreferences plainTextSharedPreferences,
                       @NonNull SharedPreferences encryptedSharedPreferences,
                       @Nullable Logger logger) {
        this.context = context;
        this.plainTextSharedPreferences = plainTextSharedPreferences;
        this.encryptedSharedPreferences = encryptedSharedPreferences;
        this.logger = logger;
    }
    
    @Provides
    @Singleton
    Context provideContext() {
        return context;
    }
    
    public static SharedPreferences openSharedPreferences(Context context,
                                                          String name) {
        return getSharedPreferenceFactory(context).get(name);
    }
    
    private static Function<String, SharedPreferences> getSharedPreferenceFactory(Context context) {
        return name -> context.getSharedPreferences(getStoreName(context, name), Context.MODE_PRIVATE);
    }
    
    private static String getStoreName(Context context,
                                       String name) {
        int availableLength = MAX_FILE_NAME_LENGTH - name.length();
        String packageName = context.getPackageName();
        
        if (packageName.length() > availableLength) {
            packageName = packageName.substring(packageName.length() - availableLength - 1, packageName.length());
        }
        
        return packageName + "$" + name;
    }
    
    @Provides
    @Singleton
    @Named(PLAIN_TEXT)
    SharedPreferenceStore provideSharedPreferenceStore(@Named(BASE_64) Serialiser base64Serialiser,
                                                       @Nullable @Named(CUSTOM) Serialiser customSerialiser,
                                                       Logger logger) {
        return new SharedPreferenceStore(
                plainTextSharedPreferences,
                base64Serialiser,
                customSerialiser,
                BehaviorSubject.create(),
                logger
        );
    }
    
    @Provides
    @Singleton
    EncryptedSharedPreferenceStore provideEncryptedSharedPreferenceStore(@Named(BASE_64) Serialiser base64Serialiser,
                                                                         @Nullable @Named(CUSTOM) Serialiser customSerialiser,
                                                                         @Nullable EncryptionManager encryptionManager,
                                                                         Logger logger) {
        return new EncryptedSharedPreferenceStore(
                encryptedSharedPreferences,
                base64Serialiser,
                customSerialiser,
                BehaviorSubject.create(),
                logger,
                encryptionManager
        );
    }
    
    @Provides
    @Singleton
    LenientEncryptedStore provideLenientEncryptedStore(@Named(PLAIN_TEXT) SharedPreferenceStore plainTextStore,
                                                       EncryptedSharedPreferenceStore encryptedStore,
                                                       Logger logger) {
        return new LenientEncryptedStore(
                plainTextStore,
                encryptedStore,
                encryptedStore.isEncryptionSupported(),
                logger
        );
    }
    
    @Provides
    @Singleton
    ForgetfulEncryptedStore provideForgetfulEncryptedStore(EncryptedSharedPreferenceStore encryptedStore,
                                                           Logger logger) {
        return new ForgetfulEncryptedStore(
                encryptedStore,
                encryptedStore.isEncryptionSupported(),
                logger
        );
    }
    
    @Provides
    @Singleton
    @Named(PLAIN_TEXT)
    KeyValueStore provideStore(@Named(PLAIN_TEXT) SharedPreferenceStore store) {
        return store;
    }
    
    @Provides
    @Singleton
    @Named(ENCRYPTED)
    KeyValueStore provideEncryptedStore(EncryptedSharedPreferenceStore store) {
        return store;
    }
    
    @Provides
    @Singleton
    @Named(LENIENT)
    KeyValueStore provideLenientStore(LenientEncryptedStore store) {
        return store;
    }
    
    @Provides
    @Singleton
    @Named(FORGETFUL)
    KeyValueStore provideForgetfulStore(ForgetfulEncryptedStore store) {
        return store;
    }
    
    @Provides
    @Singleton
    @NonNull
    Logger provideLogger() {
        return logger == null ? new SimpleLogger() : logger;
    }
    
}
