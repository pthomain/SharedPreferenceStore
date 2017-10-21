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

package uk.co.glass_software.android.shared_preferences.demo;

import java.util.Date;

import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry;

enum Keys implements StoreEntry.UniqueKeyProvider, StoreEntry.ValueClassProvider {
    
    FIRST_NAME(String.class),
    LAST_NAME(String.class),
    EMAIL(String.class),
    AGE(Integer.class),
    JOIN_DATE(Date.class),
    ADDRESS(String.class);
    
    private final String prefix = getClass().getSimpleName();
    private final Class<?> valueClass;
    
    Keys(Class valueClass) {
        this.valueClass = valueClass;
    }
    
    @Override
    public String getUniqueKey() {
        return prefix + "." + this;
    }
    
    @Override
    public Class getValueClass() {
        return valueClass;
    }
}
