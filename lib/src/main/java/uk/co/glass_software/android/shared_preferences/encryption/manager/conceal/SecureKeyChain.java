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

import com.facebook.android.crypto.keychain.SecureRandomFix;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;

import java.security.SecureRandom;

import uk.co.glass_software.android.shared_preferences.encryption.manager.key.RsaEncryptedKeyPairProvider;

public class SecureKeyChain implements KeyChain {
    
    private final CryptoConfig mCryptoConfig;
    private final SecureRandom mSecureRandom;
    private final RsaEncryptedKeyPairProvider keyPairProvider;
    
    SecureKeyChain(CryptoConfig config,
                   RsaEncryptedKeyPairProvider keyPairProvider) {
        this.keyPairProvider = keyPairProvider;
        mSecureRandom = SecureRandomFix.createLocalSecureRandom();
        mCryptoConfig = config;
    }
    
    @Override
    public synchronized byte[] getCipherKey() throws KeyChainException {
        try {
            return keyPairProvider.getCipherKey();
        }
        catch (Exception e) {
            throw new KeyChainException("Could not retrieve cipher key", e);
        }
    }
    
    @Override
    public byte[] getMacKey() throws KeyChainException {
        try {
            return keyPairProvider.getMacKey();
        }
        catch (Exception e) {
            throw new KeyChainException("Could not retrieve MAC key", e);
        }
    }
    
    @Override
    public byte[] getNewIV() throws KeyChainException {
        byte[] iv = new byte[mCryptoConfig.ivLength];
        mSecureRandom.nextBytes(iv);
        return iv;
    }
    
    @Override
    public synchronized void destroyKeys() {
        keyPairProvider.destroyKeys();
    }
    
    public boolean isEncryptionKeySecure() {
        return keyPairProvider.isEncryptionKeySecure();
    }
}
