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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;

@SuppressWarnings("unchecked")
public class SharedPreferenceStore implements KeyValueStore {
    
    private final SharedPreferences sharedPreferences;
    private final Base64Serialiser serialiser;
    final BehaviorSubject<String> changeSubject;
    final Logger logger;
    
    private Map<String, Object> cacheMap;
    
    public SharedPreferenceStore(@NonNull SharedPreferences sharedPreferences,
                                 @NonNull Base64Serialiser base64Serialiser,
                                 @NonNull BehaviorSubject<String> changeSubject,
                                 @NonNull Logger logger) {
        this.sharedPreferences = sharedPreferences;
        serialiser = base64Serialiser;
        this.changeSubject = changeSubject;
        this.logger = logger;
        if (cache()) {
            initCache();
        }
    }
    
    protected boolean cache() {
        return true;
    }
    
    synchronized void initCache() {
        if (cacheMap != null) {
            return;
        }
        
        logger.d(this, "Initialising cache: " + this);
        cacheMap = new HashMap<>();
        
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            Object value = entry.getValue();
            
            if (serialiser.isBase64(value)) {
                value = serialiser.deserialise((String) value);
            }
            
            Object readValue = readStoredValue(value);
            saveToCache(entry.getKey(), readValue);
            logger.d(this, entry.getKey() + " -> " + readValue);
        }
    }
    
    Object readStoredValue(Object value) {
        return value;
    }
    
    public Observable<String> observeChanges() {
        return changeSubject;
    }
    
    @Override
    public synchronized void deleteValue(@NonNull String key) {
        saveValue(key, null);
    }
    
    @Override
    public synchronized void saveValue(@NonNull String key,
                                       @Nullable Object value) {
        saveValue(key, value, false);
    }
    
    private void saveValue(@NonNull String key,
                           @Nullable Object value,
                           boolean isBase64) {
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
        else if (value.equals(getValue(key, value.getClass(), null))) {
            //value hasn't changed
            return;
        }
        
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
        else if (Serializable.class.isAssignableFrom(value.getClass())) {
            saveValue(key, serialiser.serialise((Serializable) value), true);
        }
        else {
            throw new IllegalArgumentException("Value of type "
                                               + value.getClass().getSimpleName()
                                               + " does not implement Serializable");
        }
        
        if (!isBase64 && cache()) {
            saveToCache(key, value);
            logger.d(this, "Saving entry " + key + " -> " + value);
            changeSubject.onNext(key);
        }
    }
    
    @Override
    @Nullable
    public synchronized <O> O getValue(@NonNull String key,
                                       @NonNull Class<O> objectClass,
                                       @Nullable O defaultValue) {
        if (hasValue(key)) {
            return getFromCache(key);
        }
        
        try {
            if (sharedPreferences.contains(key)) {
                O value;
                
                if (Boolean.class.isAssignableFrom(objectClass)
                    || boolean.class.isAssignableFrom(objectClass)) {
                    value = (O) Boolean.valueOf(sharedPreferences.getBoolean(key,
                                                                             (Boolean) defaultValue
                    ));
                }
                else if (Float.class.isAssignableFrom(objectClass)
                         || float.class.isAssignableFrom(objectClass)) {
                    value = (O) Float.valueOf(sharedPreferences.getFloat(key,
                                                                         (Float) defaultValue
                    ));
                }
                else if (Long.class.isAssignableFrom(objectClass)
                         || long.class.isAssignableFrom(objectClass)) {
                    value = (O) Long.valueOf(sharedPreferences.getLong(key, (Long) defaultValue));
                }
                else if (Integer.class.isAssignableFrom(objectClass)
                         || int.class.isAssignableFrom(objectClass)) {
                    value = (O) Integer.valueOf(sharedPreferences.getInt(key,
                                                                         (Integer) defaultValue
                    ));
                }
                else if (String.class.isAssignableFrom(objectClass)) {
                    value = (O) sharedPreferences.getString(key, (String) defaultValue);
                }
                else {
                    value = serialiser.deserialise(getValue(key, String.class, null));
                }
                
                if (cache()) {
                    saveToCache(key, value);
                }
                return value;
            }
        }
        catch (Exception e) {
            logger.e(this, e, e.getMessage());
        }
        
        return defaultValue;
    }
    
    synchronized void saveToCache(@NonNull String key,
                                  @Nullable Object value) {
        if (value == null) {
            if (hasValue(key)) {
                cacheMap.remove(key);
            }
        }
        else {
            cacheMap.put(key, value);
        }
    }
    
    @Override
    public synchronized boolean hasValue(@NonNull String key) {
        return cacheMap.containsKey(key);
    }
    
    @Nullable
    <O> O getFromCache(@NonNull String key) {
        return (O) cacheMap.get(key);
    }
    
    public synchronized Map<String, Object> getCachedValues() {
        return Collections.unmodifiableMap(cacheMap);
    }
    
}