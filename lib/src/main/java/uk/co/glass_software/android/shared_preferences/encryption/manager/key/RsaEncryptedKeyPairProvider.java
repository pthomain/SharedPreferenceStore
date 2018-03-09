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

package uk.co.glass_software.android.shared_preferences.encryption.manager.key;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.facebook.android.crypto.keychain.SecureRandomFix;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.MacConfig;

import java.security.SecureRandom;

import uk.co.glass_software.android.shared_preferences.Logger;

public class RsaEncryptedKeyPairProvider {
    
    private final static String DELIMITER = "~";
    
    private final RsaEncrypter rsaEncrypter;
    private final Logger logger;
    private final KeyPair keyPair;
    private final CryptoConfig cryptoConfig;
    private Pair pair;
    
    RsaEncryptedKeyPairProvider(RsaEncrypter rsaEncrypter,
                                Logger logger,
                                KeyPair keyPair,
                                CryptoConfig cryptoConfig) {
        this.rsaEncrypter = rsaEncrypter;
        this.logger = logger;
        this.keyPair = keyPair;
        this.cryptoConfig = cryptoConfig;
    }
    
    public void initialise() {
        try {
            getOrGenerate();
        }
        catch (Exception e) {
            logger.e(this, e, "Could not initialise RsaEncryptedKeyPairProvider");
        }
    }
    
    public synchronized byte[] getCipherKey() throws Exception {
        return getOrGenerate().cipherKey;
    }
    
    public synchronized byte[] getMacKey() throws Exception {
        return getOrGenerate().macKey;
    }
    
    public synchronized void destroyKeys() {
        pair = null;
        keyPair.drop();
    }
    
    private synchronized Pair getOrGenerate() throws Exception {
        if (pair == null) {
            String string = keyPair.get();
            
            if (string == null) {
                pair = generateNewKeyPair();
                boolean isEncrypted = isEncrypted(pair);
                
                byte[] cipherKey = isEncrypted ? pair.encryptedCipherKey : pair.cipherKey;
                byte[] macKey = isEncrypted ? pair.encryptedMacKey : pair.macKey;
                
                keyPair.save((isEncrypted ? "1" : "0")
                             + DELIMITER
                             + toBase64(cipherKey)
                             + DELIMITER
                             + toBase64(macKey)
                );
                
                return pair;
            }
            else {
                String[] strings = string.split(DELIMITER);
                boolean isKeyPairEncrypted = "1".equals(strings[0]);
                byte[] storedCipherKey = fromBase64(strings[1]);
                byte[] storedMacKey = fromBase64(strings[2]);
                
                if (isKeyPairEncrypted) {
                    pair = new Pair(
                            rsaEncrypter.decrypt(storedCipherKey),
                            rsaEncrypter.decrypt(storedMacKey),
                            storedCipherKey,
                            storedMacKey
                    );
                }
                else {
                    pair = new Pair(
                            storedCipherKey,
                            storedMacKey,
                            null,
                            null
                    );
                }
            }
        }
        
        return pair;
    }
    
    private boolean isEncrypted(Pair pair) {
        return pair.encryptedCipherKey != null && pair.encryptedMacKey != null;
    }
    
    private synchronized Pair generateNewKeyPair() throws Exception {
        byte[] cipherKey = new byte[cryptoConfig.keyLength];
        byte[] macKey = new byte[MacConfig.DEFAULT.keyLength];
        
        SecureRandom secureRandom = SecureRandomFix.createLocalSecureRandom();
        secureRandom.nextBytes(cipherKey);
        secureRandom.nextBytes(macKey);
        
        byte[] encryptedCipherKey = rsaEncrypter.encrypt(cipherKey);
        byte[] encryptedMacKey = rsaEncrypter.encrypt(macKey);
        
        if (encryptedCipherKey == null || encryptedMacKey == null) {
            logger.e(this, "RSA encrypter could not encrypt the keys");
            encryptedCipherKey = null;
            encryptedMacKey = null;
        }
        
        return new Pair(
                cipherKey,
                macKey,
                encryptedCipherKey,
                encryptedMacKey
        );
    }
    
    private String toBase64(@NonNull byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    
    private byte[] fromBase64(@NonNull String string) {
        return Base64.decode(string, Base64.DEFAULT);
    }
    
    public boolean isEncryptionKeySecure() {
        try {
            return isEncrypted(getOrGenerate());
        }
        catch (Exception e) {
            logger.e(this, e, "Could not check if the key pair was encrypted");
            return false;
        }
    }
    
    private class Pair {
        private final byte[] cipherKey;
        private final byte[] macKey;
        private final byte[] encryptedCipherKey;
        private final byte[] encryptedMacKey;
        
        private Pair(byte[] cipherKey,
                     byte[] macKey,
                     byte[] encryptedCipherKey,
                     byte[] encryptedMacKey) {
            this.cipherKey = cipherKey;
            this.macKey = macKey;
            this.encryptedCipherKey = encryptedCipherKey;
            this.encryptedMacKey = encryptedMacKey;
        }
    }
    
}