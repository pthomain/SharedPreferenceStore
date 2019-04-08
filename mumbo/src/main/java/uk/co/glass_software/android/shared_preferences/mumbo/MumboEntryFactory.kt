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
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager
import uk.co.glass_software.android.shared_preferences.mumbo.store.StoreMode
import uk.co.glass_software.android.shared_preferences.mumbo.store.StoreMode.*
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.persistence.base.UniqueKeyProvider
import uk.co.glass_software.android.shared_preferences.persistence.base.ValueClassProvider
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry

class MumboEntryFactory internal constructor(logger: Logger,
                                             val plainTextStore: KeyValueStore,
                                             val encryptedStore: KeyValueStore,
                                             val lenientStore: KeyValueStore,
                                             val forgetfulStore: KeyValueStore,
                                             encryptionManager: EncryptionManager) {
    init {
        logger.d(
                this,
                "Encryption supported: ${if (encryptionManager.isEncryptionSupported) "TRUE" else "FALSE"}"
        )
    }

    fun <C> open(key: String,
                 mode: StoreMode,
                 valueClass: Class<C>): StoreEntry<C> =
            when (mode) {
                PLAIN_TEXT -> plainTextStore
                ENCRYPTED -> encryptedStore
                LENIENT -> lenientStore
                FORGETFUL -> forgetfulStore
            }.let {
                open(
                        it,
                        object : UniqueKeyProvider {
                            override val uniqueKey = key
                        },
                        object : ValueClassProvider<C> {
                            override val valueClass = valueClass
                        }
                )
            }

    private fun <C> open(store: KeyValueStore,
                         keyProvider: UniqueKeyProvider,
                         valueClassProvider: ValueClassProvider<C>) =
            StoreEntry(store, keyProvider, valueClassProvider)

    companion object {
        fun builder(context: Context) = MumboEntryFactoryBuilder(
                context.applicationContext
        )
    }
}
