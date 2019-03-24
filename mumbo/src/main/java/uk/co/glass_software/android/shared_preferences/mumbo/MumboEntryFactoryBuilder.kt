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
import uk.co.glass_software.android.boilerplate.Boilerplate
import uk.co.glass_software.android.boilerplate.utils.log.Logger
import uk.co.glass_software.android.mumbo.Mumbo
import uk.co.glass_software.android.shared_preferences.StoreEntryFactory
import uk.co.glass_software.android.shared_preferences.mumbo.MemoryCache.*
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.MumboEncryptionManager
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser


class MumboEntryFactoryBuilder internal constructor(
        private val context: Context,
        private val isDebug: Boolean
) {

    private var preferencesFileName: String = "shared_preference_store"
    private var logger: Logger? = null
    private var customSerialiser: Serialiser? = null
    private var encryptionManager: EncryptionManager? = null
    private var isMemoryCacheEnabled: MemoryCache = BOTH

    fun preferencesFileName(preferencesFileName: String) = apply {
        this.preferencesFileName = preferencesFileName
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

    fun setMemoryCacheEnabled(isMemoryCacheEnabled: MemoryCache = BOTH) = apply {
        this.isMemoryCacheEnabled = isMemoryCacheEnabled
    }

    fun build(): MumboEntryFactory {
        val mumboPreferencesFileName = "mumbo_$preferencesFileName"

        val plainTextStore = newStoreEntryFactory(
                preferencesFileName,
                isMemoryCacheEnabled != ENCRYPTED_ONLY
        ).store

        val delegatedStore = newStoreEntryFactory(
                mumboPreferencesFileName,
                false
        ).store

        val logger = logger ?: Boilerplate.logger
        val encryptionManager = getEncryptionManager(logger)

        val component = DaggerMumboStoreComponent
                .builder()
                .mumboStoreModule(MumboStoreModule(
                        context,
                        logger,
                        plainTextStore,
                        delegatedStore,
                        encryptionManager,
                        customSerialiser,
                        isMemoryCacheEnabled != PLAIN_TEXT_ONLY
                )).build()

        return MumboEntryFactory(
                component.logger(),
                component.plainTextStore(),
                component.encryptedStore(),
                component.lenientStore(),
                component.forgetfulStore(),
                encryptionManager
        )
    }

    private fun newStoreEntryFactory(preferencesFileName: String,
                                     isMemoryCacheEnabled: Boolean): StoreEntryFactory {
        return StoreEntryFactory.builder(context, isDebug).apply {
            customSerialiser?.let { customSerialiser(it) }
            logger?.let { logger(it) }
            preferences(preferencesFileName)
            setMemoryCacheEnabled(isMemoryCacheEnabled)
        }.build()
    }

    private fun getEncryptionManager(logger: Logger) =
            encryptionManager ?: MumboEncryptionManager(Mumbo(context, logger).conceal())

}
