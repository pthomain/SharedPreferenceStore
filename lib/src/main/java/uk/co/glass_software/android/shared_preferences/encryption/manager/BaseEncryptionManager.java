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

package uk.co.glass_software.android.shared_preferences.encryption.manager;

import android.support.annotation.Nullable;
import android.util.Base64;

import uk.co.glass_software.android.shared_preferences.Logger;

abstract class BaseEncryptionManager implements EncryptionManager {
    
    protected final Logger logger;
    
    BaseEncryptionManager(Logger logger) {
        this.logger = logger;
    }
    
    @Nullable
    @Override
    public final String encrypt(String toEncrypt,
                                String dataTag) {
        if (toEncrypt == null) {
            return null;
        }
        byte[] input = encryptBytes(toEncrypt.getBytes(), dataTag);
        return input == null ? null : Base64.encodeToString(input, Base64.DEFAULT);
    }
    
    @Nullable
    @Override
    public final String decrypt(String toDecrypt,
                                String dataTag) {
        if (toDecrypt == null) {
            return null;
        }
        byte[] decode = Base64.decode(toDecrypt.getBytes(), Base64.DEFAULT);
        byte[] bytes = decryptBytes(decode, dataTag);
        return bytes == null ? null : new String(bytes);
    }
    
}
