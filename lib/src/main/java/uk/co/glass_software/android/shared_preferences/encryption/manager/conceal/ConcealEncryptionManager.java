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

package uk.co.glass_software.android.shared_preferences.encryption.manager.conceal;


import android.content.Context;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.keychain.KeyChain;
import com.facebook.soloader.SoLoader;

import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.BaseEncryptionManager;

public class ConcealEncryptionManager extends BaseEncryptionManager {
    
    private final boolean isAvailable;
    private final Crypto crypto;
    private Logger logger;
    
    ConcealEncryptionManager(Context context,
                             Logger logger,
                             KeyChain keyChain,
                             AndroidConceal androidConceal) {
        super(logger);
        this.logger = logger;
        SoLoader.init(context, false);
        
        // Creates a new Crypto object with default implementations of a key chain
        crypto = androidConceal.createDefaultCrypto(keyChain);
        
        // Check for whether the crypto functionality is available
        // This might fail if Android does not load libraries correctly.
        isAvailable = crypto.isAvailable();
        logger.d(this, "Conceal is" + (isAvailable ? "" : " NOT") + " available");
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    @Override
    public byte[] encryptBytes(byte[] toEncrypt,
                               String dataTag) {
        if (toEncrypt == null) {
            return null;
        }
        
        try {
            return crypto.encrypt(toEncrypt, Entity.create(""));
        }
        catch (Exception e) {
            logger.e(this, e, "Could not encrypt the given bytes");
            return null;
        }
    }
    
    @Override
    public byte[] decryptBytes(byte[] toDecrypt,
                               String dataTag) {
        if (toDecrypt == null) {
            return null;
        }
        
        try {
            return crypto.decrypt(toDecrypt, Entity.create(""));
        }
        catch (Exception e) {
            logger.e(this, e, "Could not decrypt the given bytes");
            return null;
        }
    }
}
