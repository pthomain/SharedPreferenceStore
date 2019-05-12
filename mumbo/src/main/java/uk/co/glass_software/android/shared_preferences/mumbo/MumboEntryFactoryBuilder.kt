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
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.mumbo.Mumbo
import uk.co.glass_software.android.mumbo.base.EncryptionManager
import uk.co.glass_software.android.shared_preferences.StoreEntryFactory
import uk.co.glass_software.android.shared_preferences.mumbo.MemoryCache.*
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.MumboEncryptionManager
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser
import uk.co.glass_software.android.shared_preferences.utils.VoidLogger


class MumboEntryFactoryBuilder internal constructor(private val context: Context) {

    private var preferencesFileName: String = "shared_preference_store"
    private var logger: Logger? = null
    private var customSerialiser: Serialiser? = null
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

    fun setMemoryCacheEnabled(isMemoryCacheEnabled: MemoryCache = BOTH) = apply {
        this.isMemoryCacheEnabled = isMemoryCacheEnabled
    }

    fun build(mumboPicker: (Mumbo) -> EncryptionManager): MumboEntryFactory {
        val mumboPreferencesFileName = "mumbo_$preferencesFileName"

        val plainTextStore = newStoreEntryFactory(
                preferencesFileName,
                isMemoryCacheEnabled != ENCRYPTED_ONLY
        ).store

        val delegatedStore = newStoreEntryFactory(
                mumboPreferencesFileName,
                false
        ).store

        val logger = logger ?: VoidLogger()
        val mumbo = Mumbo(context, logger)
        val encryptionManager = MumboEncryptionManager(mumboPicker(mumbo))

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
        return StoreEntryFactory.builder(context).apply {
            customSerialiser?.let { customSerialiser(it) }
            logger?.let { logger(it) }
            preferences(preferencesFileName)
            setMemoryCacheEnabled(isMemoryCacheEnabled)
        }.build()
    }

}
