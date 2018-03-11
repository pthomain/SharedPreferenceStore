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
import com.facebook.android.crypto.keychain.SecureRandomFix;
import com.facebook.crypto.CryptoConfig;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyModule;
import uk.co.glass_software.android.shared_preferences.encryption.manager.key.RsaEncryptedKeyPairProvider;

@Module(includes = KeyModule.class)
public class ConcealModule {
    
    @Provides
    @Singleton
    SecureKeyChain provideKeyChain(CryptoConfig cryptoConfig,
                                   RsaEncryptedKeyPairProvider keyPairProvider) {
        return new SecureKeyChain(
                cryptoConfig,
                keyPairProvider,
                SecureRandomFix::createLocalSecureRandom
        );
    }
    
    @Provides
    @Singleton
    ConcealEncryptionManager provideConcealEncryptionManager(Logger logger,
                                                             SecureKeyChain keyChain,
                                                             Context applicationContext) {
        return new ConcealEncryptionManager(
                applicationContext,
                logger,
                keyChain,
                AndroidConceal.get()
        );
    }
    
}
