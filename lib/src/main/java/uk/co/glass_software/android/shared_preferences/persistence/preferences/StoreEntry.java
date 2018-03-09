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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;

public class StoreEntry<C> implements KeyValueEntry<C> {
    
    private final KeyValueStore store;
    private final Class<C> valueClass;
    private final C defaultValue;
    private final String keyString;
    
    public StoreEntry(@NonNull KeyValueStore store,
                      @NonNull String key,
                      @NonNull Class<C> valueClass) {
        this(store, key, valueClass, null);
    }
    
    public StoreEntry(@NonNull KeyValueStore store,
                      @NonNull String key,
                      @NonNull Class<C> valueClass,
                      @Nullable C defaultValue) {
        this(store, () -> key, () -> valueClass, defaultValue);
    }
    
    public <K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider> StoreEntry(@NonNull KeyValueStore store,
                                                                                               @NonNull K keyProvider) {
        this(store, keyProvider, keyProvider);
    }
    
    public <K extends StoreEntry.UniqueKeyProvider & StoreEntry.ValueClassProvider> StoreEntry(@NonNull KeyValueStore store,
                                                                                               @NonNull K keyProvider,
                                                                                               @Nullable C defaultValue) {
        this(store, keyProvider, keyProvider, defaultValue);
    }
    
    public StoreEntry(@NonNull KeyValueStore store,
                      @NonNull StoreEntry.UniqueKeyProvider keyProvider,
                      @NonNull StoreEntry.ValueClassProvider valueClassProvider) {
        this(store, keyProvider, valueClassProvider, null);
    }
    
    public StoreEntry(@NonNull KeyValueStore store,
                      @NonNull StoreEntry.UniqueKeyProvider keyProvider,
                      @NonNull StoreEntry.ValueClassProvider valueClassProvider,
                      @Nullable C defaultValue) {
        this.store = store;
        this.keyString = keyProvider.getUniqueKey();
        this.valueClass = valueClassProvider.getValueClass();
        this.defaultValue = defaultValue;
    }
    
    @Override
    public final synchronized void save(@Nullable C value) {
        store.saveValue(getKey(), value);
    }
    
    @Override
    @Nullable
    public final synchronized C get() {
        return get(defaultValue);
    }
    
    @Override
    @Nullable
    public final synchronized C get(@Nullable C defaultValue) {
        return store.getValue(getKey(), valueClass, defaultValue);
    }
    
    @Override
    public final synchronized void drop() {
        store.deleteValue(getKey());
    }
    
    @Override
    @NonNull
    public final String getKey() {
        return keyString;
    }
    
    @Override
    public final boolean exists() {
        return store.hasValue(getKey());
    }
    
    public interface UniqueKeyProvider {
        String getUniqueKey();
    }
    
    public interface ValueClassProvider {
        Class getValueClass();
    }
}

