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

package uk.co.glass_software.android.shared_preferences.encryption.manager.custom;

import android.support.annotation.NonNull;

import java.security.Key;

import javax.crypto.Cipher;

import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.BaseCustomEncryptionManager;

//see https://medium.com/@ericfu/securely-storing-secrets-in-an-android-application-501f030ae5a3#.qcgaaeaso
public class PreMEncryptionManager extends BaseCustomEncryptionManager {

    private static final String ENCRYPTION_PROVIDER = "BC";
    private static final String AES_MODE = "AES/ECB/PKCS7Padding";
    
    private final SecureKeyProvider secureKeyProvider;
    
    PreMEncryptionManager(Logger logger,
                          SecureKeyProvider secureKeyProvider){
        super(logger);
        this.secureKeyProvider = secureKeyProvider;
    }
    
    @NonNull
    @Override
    protected Cipher getCipher(boolean isEncrypt) throws Exception {
        Key secretKey = secureKeyProvider.getKey();
        if (secretKey == null) {
            throw new IllegalStateException("Could not retrieve the secret key");
        }
        else {
            Cipher cipher = Cipher.getInstance(AES_MODE, ENCRYPTION_PROVIDER);
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
                        secretKey
            );
            return cipher;
        }
    }
    
}
