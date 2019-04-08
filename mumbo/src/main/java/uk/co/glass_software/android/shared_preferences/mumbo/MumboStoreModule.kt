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

package uk.co.glass_software.android.shared_preferences.mumbo

import android.content.Context
import android.util.Base64
import dagger.Module
import dagger.Provides
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager
import uk.co.glass_software.android.shared_preferences.mumbo.store.EncryptedSharedPreferenceStore
import uk.co.glass_software.android.shared_preferences.mumbo.store.ForgetfulEncryptedStore
import uk.co.glass_software.android.shared_preferences.mumbo.store.LenientEncryptedStore
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Base64Serialiser
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser
import javax.inject.Named
import javax.inject.Singleton

@Module
internal class MumboStoreModule(private val context: Context,
                                private val logger: Logger,
                                private val plainTextStore: KeyValueStore,
                                private val delegatePlainTextStore: KeyValueStore,
                                private val encryptionManager: EncryptionManager,
                                private val customSerialiser: Serialiser?,
                                private val isMemoryCacheEnabled: Boolean) {
    @Provides
    @Singleton
    fun provideContext() = context

    @Provides
    @Singleton
    @Named(PLAIN_TEXT)
    fun provideSharedPreferenceStore() = plainTextStore

    @Provides
    @Singleton
    @Named(ENCRYPTED)
    fun provideEncryptedSharedPreferenceStore(base64Serialiser: Base64Serialiser): KeyValueStore =
            EncryptedSharedPreferenceStore(
                    logger,
                    base64Serialiser,
                    customSerialiser,
                    delegatePlainTextStore,
                    encryptionManager,
                    isMemoryCacheEnabled
            )

    @Provides
    @Singleton
    @Named(LENIENT)
    fun provideLenientEncryptedStore(@Named(PLAIN_TEXT) plainTextStore: KeyValueStore,
                                     @Named(ENCRYPTED) encryptedStore: KeyValueStore,
                                     logger: Logger): KeyValueStore =
            LenientEncryptedStore(
                    plainTextStore,
                    encryptedStore,
                    encryptionManager.isEncryptionSupported,
                    logger
            )

    @Provides
    @Singleton
    @Named(FORGETFUL)
    fun provideForgetfulEncryptedStore(@Named(ENCRYPTED) encryptedStore: KeyValueStore,
                                       logger: Logger): KeyValueStore =
            ForgetfulEncryptedStore(
                    encryptedStore,
                    encryptionManager.isEncryptionSupported,
                    logger
            )

    @Provides
    @Singleton
    fun provideLogger() = logger

    @Provides
    @Singleton
    fun provideEncryptionManager() = encryptionManager

    @Provides
    @Singleton
    fun provideBase64Serialiser(logger: Logger) = Base64Serialiser(
            logger,
            object : Base64Serialiser.CustomBase64 {
                override fun encode(input: ByteArray, flags: Int) = Base64.encodeToString(input, flags)
                override fun decode(input: String, flags: Int) = Base64.decode(input, flags)
            }
    )

    companion object {
        const val PLAIN_TEXT = "plain_text"
        const val ENCRYPTED = "encrypted"
        const val LENIENT = "lenient"
        const val FORGETFUL = "forgetful"
    }

}
