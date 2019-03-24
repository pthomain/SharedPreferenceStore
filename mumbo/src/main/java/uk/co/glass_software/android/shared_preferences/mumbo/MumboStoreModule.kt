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
import io.reactivex.subjects.PublishSubject
import uk.co.glass_software.android.boilerplate.utils.log.Logger
import uk.co.glass_software.android.boilerplate.utils.preferences.Prefs
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager
import uk.co.glass_software.android.shared_preferences.mumbo.store.EncryptedSharedPreferenceStore
import uk.co.glass_software.android.shared_preferences.mumbo.store.ForgetfulEncryptedStore
import uk.co.glass_software.android.shared_preferences.mumbo.store.LenientEncryptedStore
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Base64Serialiser
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser
import javax.inject.Named
import javax.inject.Singleton

@Module
internal class MumboStoreModule(private val context: Context,
                                private val plainTextPrefs: Prefs,
                                private val encryptedPrefs: Prefs,
                                private val logger: Logger,
                                private val encryptionManager: EncryptionManager,
                                private val customSerialiser: Serialiser?) {

    @Provides
    @Singleton
    fun provideContext() = context

    @Provides
    @Singleton
    @Named(PLAIN_TEXT)
    fun provideSharedPreferenceStore(base64Serialiser: Base64Serialiser,
                                     logger: Logger): SharedPreferenceStore =
            SharedPreferenceStore(
                    plainTextPrefs,
                    base64Serialiser,
                    customSerialiser,
                    PublishSubject.create(),
                    logger
            )

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferenceStore(base64Serialiser: Base64Serialiser,
                                              logger: Logger) =
            EncryptedSharedPreferenceStore(
                    encryptedPrefs,
                    base64Serialiser,
                    customSerialiser,
                    PublishSubject.create(),
                    logger,
                    encryptionManager
            )

    @Provides
    @Singleton
    fun provideLenientEncryptedStore(@Named(PLAIN_TEXT) plainTextStore: SharedPreferenceStore,
                                     encryptedStore: EncryptedSharedPreferenceStore,
                                     logger: Logger): LenientEncryptedStore =
            LenientEncryptedStore(
                    plainTextStore,
                    encryptedStore,
                    encryptionManager.isEncryptionSupported,
                    logger
            )

    @Provides
    @Singleton
    fun provideForgetfulEncryptedStore(encryptedStore: EncryptedSharedPreferenceStore,
                                       logger: Logger): ForgetfulEncryptedStore =
            ForgetfulEncryptedStore(
                    encryptedStore,
                    encryptionManager.isEncryptionSupported,
                    logger
            )

    @Provides
    @Singleton
    @Named(PLAIN_TEXT)
    fun provideStore(@Named(PLAIN_TEXT) store: SharedPreferenceStore): KeyValueStore = store

    @Provides
    @Singleton
    @Named(ENCRYPTED)
    fun provideEncryptedStore(store: EncryptedSharedPreferenceStore): KeyValueStore = store

    @Provides
    @Singleton
    @Named(LENIENT)
    fun provideLenientStore(store: LenientEncryptedStore): KeyValueStore = store

    @Provides
    @Singleton
    @Named(FORGETFUL)
    fun provideForgetfulStore(store: ForgetfulEncryptedStore): KeyValueStore = store

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
