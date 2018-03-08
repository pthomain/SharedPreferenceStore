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

package uk.co.glass_software.android.shared_preferences.persistence.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class StoreEntry<C> {

    private final KeyValueStore store;
    private final String keyString;
    private final Class<C> valueClass;
    private final C defaultValue;

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

    public synchronized void save(@Nullable C value) {
        store.saveValue(keyString, value);
    }

    @Nullable
    public synchronized C get() {
        return get(defaultValue);
    }

    @Nullable
    public synchronized C get(@Nullable C defaultValue) {
        return store.getValue(keyString, valueClass, defaultValue);
    }

    public synchronized void drop() {
        store.deleteValue(keyString);
    }

    @NonNull
    public String getKey() {
        return keyString;
    }

    public boolean exists() {
        return store.hasValue(keyString);
    }

    public interface UniqueKeyProvider {
        String getUniqueKey();
    }

    public interface ValueClassProvider {
        Class getValueClass();
    }
}

