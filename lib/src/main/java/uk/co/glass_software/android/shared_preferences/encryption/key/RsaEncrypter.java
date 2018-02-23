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

package uk.co.glass_software.android.shared_preferences.encryption.key;

import android.support.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

class RsaEncrypter {
    
    private static final String CIPHER_PROVIDER = "AndroidOpenSSL";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
    
    @Nullable
    private final KeyStore keyStore;
    
    private final String alias;
    
    RsaEncrypter(@Nullable KeyStore keyStore,
                 String alias) {
        this.keyStore = keyStore;
        this.alias = alias;
    }
    
    @Nullable
    byte[] encrypt(byte[] secret) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = getPrivateKeyEntry();
    
        if (privateKeyEntry == null) {
            return null;
        }
        
        Cipher inputCipher = getCipherInstance();
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        cipherOutputStream.write(secret);
        cipherOutputStream.close();
        
        return outputStream.toByteArray();
    }
    
    @Nullable
    byte[] decrypt(byte[] encrypted) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = getPrivateKeyEntry();
        
        if (privateKeyEntry == null) {
            return null;
        }
        
        Cipher outputCipher = getCipherInstance();
        outputCipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(encrypted);
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, outputCipher);
        ArrayList<Byte> values = new ArrayList<>();
        
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }
        
        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }
        return bytes;
    }
    
    private Cipher getCipherInstance() throws NoSuchAlgorithmException,
                                              NoSuchProviderException,
                                              NoSuchPaddingException {
        return Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER);
    }
    
    @Nullable
    private KeyStore.PrivateKeyEntry getPrivateKeyEntry() throws NoSuchAlgorithmException,
                                                                 UnrecoverableEntryException,
                                                                 KeyStoreException {
        return keyStore == null
               ? null
               : (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                       alias,
                       null
               );
    }
    
}
