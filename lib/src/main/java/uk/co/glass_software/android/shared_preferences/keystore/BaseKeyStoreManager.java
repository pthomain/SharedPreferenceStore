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

package uk.co.glass_software.android.shared_preferences.keystore;

import android.support.annotation.Nullable;
import android.util.Base64;

import javax.crypto.Cipher;

import uk.co.glass_software.android.shared_preferences.Logger;

public abstract class BaseKeyStoreManager implements KeyStoreManager {
    
    protected final Logger logger;
    
    BaseKeyStoreManager(Logger logger) {
        this.logger = logger;
    }
    
    @Nullable
    @Override
    public String encrypt(String toEncrypt) {
        if (toEncrypt == null) {
            return null;
        }
        byte[] input = encryptBytes(toEncrypt.getBytes());
        return input == null ? null : Base64.encodeToString(input, Base64.DEFAULT);
    }
    
    @Nullable
    @Override
    public String decrypt(String toDecrypt) {
        if (toDecrypt == null) {
            return null;
        }
        byte[] decode = Base64.decode(toDecrypt.getBytes(), Base64.DEFAULT);
        byte[] bytes = decryptBytes(decode);
        return bytes == null ? null : new String(bytes);
    }
    
    @Override
    @Nullable
    public byte[] encryptBytes(byte[] toEncrypt) {
        if (toEncrypt == null) {
            return null;
        }
        
        createNewKeyPairIfNeeded();
        
        try {
            Cipher cipher = getCipher(true);
            return cipher.doFinal(toEncrypt);
        }
        catch (Exception e) {
            logger.e(this, e, "Could not encrypt the given bytes");
            return null;
        }
    }
    
    @Override
    public byte[] decryptBytes(byte[] toDecrypt) {
        if (toDecrypt == null) {
            return null;
        }
        
        try {
            Cipher cipher = getCipher(false);
            return cipher.doFinal(toDecrypt);
        }
        catch (Exception e) {
            logger.e(this, "Could not decrypt the given bytes");
            return null;
        }
    }
    
    protected abstract void createNewKeyPairIfNeeded();
    
    protected abstract Cipher getCipher(boolean isEncrypt) throws Exception;
    
}
