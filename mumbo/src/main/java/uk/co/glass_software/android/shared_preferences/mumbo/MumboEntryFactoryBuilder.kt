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
import uk.co.glass_software.android.boilerplate.utils.log.Logger
import uk.co.glass_software.android.boilerplate.utils.preferences.Prefs
import uk.co.glass_software.android.mumbo.Mumbo
import uk.co.glass_software.android.shared_preferences.mumbo.MumboEntryFactory.Companion.DEFAULT_ENCRYPTED_PREFERENCE_NAME
import uk.co.glass_software.android.shared_preferences.mumbo.MumboEntryFactory.Companion.DEFAULT_PLAIN_TEXT_PREFERENCE_NAME
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.MumboEncryptionManager
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreUtils.openSharedPreferences
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser


class MumboEntryFactoryBuilder internal constructor(private val context: Context) {

    private var plainTextPreferences: Prefs? = null
    private var encryptedPreferences: Prefs? = null
    private var logger: Logger? = null
    private var customSerialiser: Serialiser? = null
    private var encryptionManager: EncryptionManager? = null

    fun plainTextPreferences(preferencesFileName: String) = apply {
        this.plainTextPreferences = Prefs.with(preferencesFileName)
    }

    fun encryptedPreferences(preferencesFileName: String) = apply {
        this.encryptedPreferences = Prefs.with(preferencesFileName)
    }

    fun logger(logger: Logger) = apply {
        this.logger = logger
    }

    fun customSerialiser(customSerialiser: Serialiser) = apply {
        this.customSerialiser = customSerialiser
    }

    fun encryptionManager(encryptionManager: EncryptionManager) = apply {
        this.encryptionManager = encryptionManager
    }

    private fun noLogger() = object : Logger {
        override fun d(tagOrCaller: Any, message: String) = Unit
        override fun e(tagOrCaller: Any, message: String) = Unit
        override fun e(tagOrCaller: Any, t: Throwable, message: String?) = Unit
    }

    fun build(): MumboEntryFactory {
        val logger = logger ?: noLogger()

        val component = DaggerMumboStoreComponent
                .builder()
                .mumboStoreModule(MumboStoreModule(
                        context,
                        plainTextPreferences
                                ?: openSharedPreferences(context, DEFAULT_PLAIN_TEXT_PREFERENCE_NAME),
                        encryptedPreferences
                                ?: openSharedPreferences(context, DEFAULT_ENCRYPTED_PREFERENCE_NAME),
                        logger,
                        getEncryptionManager(logger),
                        customSerialiser
                ))
                .build()

        return MumboEntryFactory(
                component.logger(),
                component.store(),
                component.encryptedStore(),
                component.lenientStore(),
                component.forgetfulStore(),
                component.encryptionManager()
        )
    }

    private fun getEncryptionManager(logger: Logger) =
            encryptionManager ?: MumboEncryptionManager(Mumbo(context, logger).conceal())

}
