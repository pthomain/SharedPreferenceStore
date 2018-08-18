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

package uk.co.glass_software.android.shared_preferences.encryption.manager.conceal

import android.content.Context

import com.facebook.android.crypto.keychain.AndroidConceal
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.CryptoConfig

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import uk.co.glass_software.android.boilerplate.log.Logger

@Module
internal class ConcealModule {

    @Provides
    @Singleton
    fun provideKeyChain(cryptoConfig: CryptoConfig,
                        context: Context): SharedPrefsBackedKeyChain {
        return SharedPrefsBackedKeyChain(
                context,
                cryptoConfig
        )
    }

    @Provides
    @Singleton
    fun provideCryptoConfig(): CryptoConfig {
        return CryptoConfig.KEY_256
    }

    @Provides
    @Singleton
    fun provideConcealEncryptionManager(logger: Logger,
                                        keyChain: SharedPrefsBackedKeyChain,
                                        applicationContext: Context): ConcealEncryptionManager {
        return ConcealEncryptionManager(
                applicationContext,
                logger,
                keyChain,
                AndroidConceal.get()
        )
    }

}
