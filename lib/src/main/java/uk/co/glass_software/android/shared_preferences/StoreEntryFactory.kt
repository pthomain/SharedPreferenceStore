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

package uk.co.glass_software.android.shared_preferences

import android.content.Context
import uk.co.glass_software.android.boilerplate.log.Logger
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManager
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry.UniqueKeyProvider
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry.ValueClassProvider
import uk.co.glass_software.android.shared_preferences.utils.StoreKey
import uk.co.glass_software.android.shared_preferences.utils.StoreMode
import uk.co.glass_software.android.shared_preferences.utils.StoreMode.*

class StoreEntryFactory internal constructor(logger: Logger,
                                             val plainTextStore: KeyValueStore,
                                             val encryptedStore: KeyValueStore,
                                             val lenientStore: KeyValueStore,
                                             val forgetfulStore: KeyValueStore,
                                             encryptionManager: EncryptionManager?) {
    init {
        logger.d(
                this,
                "Encryption supported: ${if (encryptionManager?.isEncryptionSupported == true) "TRUE" else "FALSE"}"
        )
    }

    fun <C> open(keyHolder: StoreKey.Holder): StoreEntry<C> = open(keyHolder.key)

    fun <C> open(key: StoreKey): StoreEntry<C> =
            open(
                    key.uniqueKey,
                    key.mode,
                    key.valueClass as Class<C>
            )

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
                        object : ValueClassProvider {
                            override val valueClass = valueClass
                        }
                )
            }

    private fun <C> open(store: KeyValueStore,
                         keyProvider: UniqueKeyProvider,
                         valueClassProvider: ValueClassProvider) =
            StoreEntry<C>(store, keyProvider, valueClassProvider)

    companion object {

        val DEFAULT_PLAIN_TEXT_PREFERENCE_NAME = "plain_text_store"
        val DEFAULT_ENCRYPTED_PREFERENCE_NAME = "encrypted_store"

        fun buildDefault(context: Context) = builder(context).build()

        fun builder(context: Context) = StoreEntryFactoryBuilder(
                context.applicationContext,
                BuildConfig.DEBUG
        )
    }
}
