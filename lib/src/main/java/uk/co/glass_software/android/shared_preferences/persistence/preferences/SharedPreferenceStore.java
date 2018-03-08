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

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser;

@SuppressWarnings("unchecked")
public class SharedPreferenceStore implements KeyValueStore {
    
    private final SharedPreferences sharedPreferences;
    private final BehaviorSubject<String> changeSubject;
    private final Map<String, Object> cacheMap;
    
    final Serialiser base64Serialiser;
    @Nullable final Serialiser customSerialiser;
    final Logger logger;
    
    SharedPreferenceStore(@NonNull SharedPreferences sharedPreferences,
                          @NonNull Serialiser base64Serialiser,
                          @Nullable Serialiser customSerialiser,
                          @NonNull BehaviorSubject<String> changeSubject,
                          @NonNull Logger logger) {
        this.sharedPreferences = sharedPreferences;
        this.base64Serialiser = base64Serialiser;
        this.customSerialiser = customSerialiser;
        this.changeSubject = changeSubject;
        this.logger = logger;
        cacheMap = new HashMap<>();
    }
    
    public final Observable<String> observeChanges() {
        return changeSubject;
    }
    
    @Override
    public final synchronized void deleteValue(@NonNull String key) {
        saveValue(key, null);
    }
    
    @Override
    public final synchronized void saveValue(@NonNull String key,
                                             @Nullable Object value) {
        saveToCache(key, value);
        saveValueInternal(key, value);
    }
    
    synchronized void saveValueInternal(@NonNull String key,
                                        @Nullable Object value) {
        if (value == null) {
            logger.d(this, "Deleting entry " + key);
            if (sharedPreferences.contains(key)) {
                sharedPreferences.edit().remove(key).apply();
                if (hasValue(key)) {
                    cacheMap.remove(key);
                }
                changeSubject.onNext(key);
            }
            return;
        }
        
        try {
            if (Boolean.class.isAssignableFrom(value.getClass())
                || boolean.class.isAssignableFrom(value.getClass())) {
                sharedPreferences.edit().putBoolean(key, (Boolean) value).apply();
            }
            else if (Float.class.isAssignableFrom(value.getClass())
                     || float.class.isAssignableFrom(value.getClass())) {
                sharedPreferences.edit().putFloat(key, (Float) value).apply();
            }
            else if (Long.class.isAssignableFrom(value.getClass())
                     || long.class.isAssignableFrom(value.getClass())) {
                sharedPreferences.edit().putLong(key, (Long) value).apply();
            }
            else if (Integer.class.isAssignableFrom(value.getClass())
                     || int.class.isAssignableFrom(value.getClass())) {
                sharedPreferences.edit().putInt(key, (Integer) value).apply();
            }
            else if (String.class.isAssignableFrom(value.getClass())) {
                sharedPreferences.edit().putString(key, (String) value).apply();
            }
            else {
                String serialised = serialise(key, value);
                
                if (serialised == null) {
                    throw new IllegalArgumentException("Value of type "
                                                       + value.getClass().getSimpleName()
                                                       + " does not implement Serializable");
                }
                
                sharedPreferences.edit().putString(key, serialised).apply();
            }
            
            logger.d(this, "Saving entry " + key + " -> " + value);
            changeSubject.onNext(key);
        }
        catch (Exception e) {
            logger.e(this, e, e.getMessage());
        }
    }
    
    @Override
    @Nullable
    public final synchronized <O> O getValue(@NonNull String key,
                                             @NonNull Class<O> objectClass,
                                             @Nullable O defaultValue) {
        Object fromCache = cacheMap.get(key);
        
        if (fromCache != null) {
            return (O) fromCache;
        }
        
        O valueInternal = getValueInternal(key, objectClass, defaultValue);
        saveToCache(key, valueInternal);
        return valueInternal;
    }
    
    synchronized <O> O getValueInternal(@NonNull String key,
                                        @NonNull Class<O> objectClass,
                                        @Nullable O defaultValue) {
        try {
            if (sharedPreferences.contains(key)) {
                O value;
                
                if (Boolean.class.isAssignableFrom(objectClass)
                    || boolean.class.isAssignableFrom(objectClass)) {
                    value = (O) Boolean.valueOf(sharedPreferences.getBoolean(
                            key,
                            defaultValue == null ? false : (Boolean) defaultValue
                    ));
                }
                else if (Float.class.isAssignableFrom(objectClass)
                         || float.class.isAssignableFrom(objectClass)) {
                    value = (O) Float.valueOf(sharedPreferences.getFloat(
                            key,
                            defaultValue == null ? 0f : (Float) defaultValue
                    ));
                }
                else if (Long.class.isAssignableFrom(objectClass)
                         || long.class.isAssignableFrom(objectClass)) {
                    value = (O) Long.valueOf(sharedPreferences.getLong(
                            key,
                            defaultValue == null ? 0L : (Long) defaultValue
                    ));
                }
                else if (Integer.class.isAssignableFrom(objectClass)
                         || int.class.isAssignableFrom(objectClass)) {
                    value = (O) Integer.valueOf(sharedPreferences.getInt(
                            key,
                            defaultValue == null ? 0 : (Integer) defaultValue
                    ));
                }
                else if (String.class.isAssignableFrom(objectClass)) {
                    value = (O) sharedPreferences.getString(key, (String) defaultValue);
                }
                else {
                    String serialised = sharedPreferences.getString(key, (String) defaultValue);
                    value = deserialise(key, serialised, objectClass);
                }
                
                logger.d(this, "Reading entry " + key + " -> " + value);
                return value;
            }
        }
        catch (Exception e) {
            logger.e(this, e, e.getMessage());
        }
        
        return defaultValue;
    }
    
    @Nullable
    String serialise(String key,
                     @NonNull Object value) {
        Class<?> targetClass = value.getClass();
        try {
            if (customSerialiser != null && customSerialiser.canHandleType(targetClass)) {
                return customSerialiser.serialise(value);
            }
            else if (base64Serialiser.canHandleType(targetClass)) {
                return base64Serialiser.serialise(value);
            }
        }
        catch (Serialiser.SerialisationException e) {
            logger.e(this, e, "Could not serialise " + value);
        }
        
        return null;
    }
    
    @Nullable
    <O> O deserialise(String key,
                      String serialised,
                      Class<O> objectClass) throws Serialiser.SerialisationException {
        if (serialised != null) {
            if (customSerialiser != null && customSerialiser.canHandleType(objectClass)) {
                return customSerialiser.deserialise(serialised, objectClass);
            }
            else if (base64Serialiser.canHandleType(objectClass)) {
                return base64Serialiser.deserialise(serialised, objectClass);
            }
        }
        return null;
    }
    
    private synchronized void saveToCache(@NonNull String key,
                                          @Nullable Object value) {
        if (value == null) {
            cacheMap.remove(key);
        }
        else {
            cacheMap.put(key, value);
        }
    }
    
    @Override
    public final synchronized boolean hasValue(@NonNull String key) {
        return sharedPreferences.contains(key);
    }
    
    public final synchronized Map<String, Object> getCachedValues() {
        return Collections.unmodifiableMap(cacheMap);
    }
    
}