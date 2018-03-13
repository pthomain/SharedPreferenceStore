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

package uk.co.glass_software.android.shared_preferences.utils;

import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry;

public class StoreKey implements StoreEntry.UniqueKeyProvider,
                                 StoreEntry.ValueClassProvider,
                                 StoreMode.Provider {
    private final Enum parent;
    private final StoreMode mode;
    private final Class valueClass;
    
    public StoreKey(Enum parent,
                    StoreMode mode,
                    Class valueClass) {
        this.parent = parent;
        this.mode = mode;
        this.valueClass = valueClass;
    }
    
    @Override
    public String getUniqueKey() {
        return parent.getClass().getSimpleName() + "." + parent.name();
    }
    
    @Override
    public Class getValueClass() {
        return valueClass;
    }
    
    @Override
    public StoreMode getMode() {
        return mode;
    }
    
    @Override
    public String toString() {
        return "StoreKey{" +
               "parent=" + parent +
               ", mode=" + mode +
               ", valueClass=" + valueClass +
               '}';
    }
}

