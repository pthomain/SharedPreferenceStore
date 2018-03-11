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

import android.support.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.inject.Provider;

import uk.co.glass_software.android.shared_preferences.Function;
import uk.co.glass_software.android.shared_preferences.Logger;

class RsaEncrypter {
    
    @Nullable
    private final KeyStore keyStore;
    
    private final Provider<Cipher> cipherProvider;
    private final Function<Cipher, OutputStreams> outputStreamsProvider;
    private final Function<Pair, InputStreams> inputStreamsProvider;
    private final Logger logger;
    private final String alias;
    
    RsaEncrypter(@Nullable KeyStore keyStore,
                 Provider<Cipher> cipherProvider,
                 Function<Cipher, OutputStreams> outputStreamsProvider,
                 Function<Pair, InputStreams> inputStreamsProvider,
                 Logger logger,
                 String alias) {
        this.keyStore = keyStore;
        this.cipherProvider = cipherProvider;
        this.outputStreamsProvider = outputStreamsProvider;
        this.inputStreamsProvider = inputStreamsProvider;
        this.logger = logger;
        this.alias = alias;
    }
    
    @Nullable
    byte[] encrypt(byte[] secret) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = getPrivateKeyEntry();
        
        if (privateKeyEntry == null) {
            logger.e(this, "Private key entry was null");
            return null;
        }
        
        Cipher inputCipher = cipherProvider.get();
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());
        
        OutputStreams streams = outputStreamsProvider.get(inputCipher);
        try {
            streams.cipherOutputStream.write(secret);
            return streams.outputStream.toByteArray();
        }
        finally {
            streams.outputStream.close();
            streams.cipherOutputStream.close();
        }
    }
    
    @Nullable
    byte[] decrypt(byte[] encrypted) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = getPrivateKeyEntry();
        
        if (privateKeyEntry == null) {
            logger.e(this, "Private key entry was null");
            return null;
        }
        
        Cipher outputCipher = cipherProvider.get();
        outputCipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
        
        InputStreams inputStreams = inputStreamsProvider.get(new Pair(outputCipher, encrypted));
        List<Byte> values = new LinkedList<>();
        try {
            int nextByte;
            while ((nextByte = inputStreams.cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }
            
            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i);
            }
            return bytes;
        }
        finally {
            inputStreams.inputStream.close();
            inputStreams.cipherInputStream.close();
        }
    }
    
    @Nullable
    private KeyStore.PrivateKeyEntry getPrivateKeyEntry() throws NoSuchAlgorithmException,
                                                                 UnrecoverableEntryException,
                                                                 KeyStoreException {
        if (keyStore == null) {
            logger.e(this, "KeyStore is null, no encryption on device");
            return null;
        }
        else {
            logger.d(this, "Found a key pair in the KeyStore");
            return (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                    alias,
                    null
            );
        }
    }
    
    static class OutputStreams {
        private final ByteArrayOutputStream outputStream;
        private final CipherOutputStream cipherOutputStream;
        
        OutputStreams(ByteArrayOutputStream outputStream,
                      CipherOutputStream cipherOutputStream) {
            this.outputStream = outputStream;
            this.cipherOutputStream = cipherOutputStream;
        }
    }
    
    static class InputStreams {
        private final ByteArrayInputStream inputStream;
        private final CipherInputStream cipherInputStream;
        
        InputStreams(ByteArrayInputStream inputStream,
                     CipherInputStream cipherInputStream) {
            this.inputStream = inputStream;
            this.cipherInputStream = cipherInputStream;
        }
    }
    
    static class Pair {
        final Cipher cipher;
        final byte[] encrypted;
        
        Pair(Cipher cipher,
             byte[] encrypted) {
            this.cipher = cipher;
            this.encrypted = encrypted;
        }
    }
}
