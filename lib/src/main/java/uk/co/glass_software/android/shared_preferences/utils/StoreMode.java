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

import uk.co.glass_software.android.shared_preferences.StoreEntryFactory;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry;

public enum StoreMode {
    PLAIN_TEXT,
    ENCRYPTED,
    LENIENT,  //will try to encrypt but falls back to plain-text if encryption isn't supported, see LenientEncryptedStore
    FORGETFUL;//will try to encrypt but won't save anything if encryption isn't supported, see ForgetfulEncryptedStore
    
    public final <C> StoreEntry<C> open(StoreKey key,
                                        StoreEntryFactory factory) {
        switch (this) {
            case PLAIN_TEXT:
                return factory.open(key);
            
            case ENCRYPTED:
                return factory.openEncrypted(key);
            
            case LENIENT:
                return factory.openLenient(key);
            
            case FORGETFUL:
                return factory.openForgetful(key);
        }
        
        throw new IllegalStateException("Unexpected mode: " + this);
    }
    
    public interface Provider {
        StoreMode getMode();
    }
}