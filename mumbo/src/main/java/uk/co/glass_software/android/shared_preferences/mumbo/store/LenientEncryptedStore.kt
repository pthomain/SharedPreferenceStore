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

package uk.co.glass_software.android.shared_preferences.mumbo.store

import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore

/**
 * This class will attempt to encrypt and save a given value
 * BUT will save in PLAIN-TEXT silently if encryption isn't supported.
 */
internal class LenientEncryptedStore(plainTextStore: KeyValueStore,
                                     encryptedStore: KeyValueStore,
                                     isEncryptionSupported: Boolean,
                                     logger: Logger) : KeyValueStore {
    private val internalStore = if (isEncryptionSupported) encryptedStore else plainTextStore

    init {
        logger.d(this, "Encryption is${if (isEncryptionSupported) "" else " NOT"} supported")
    }

    override fun <V> getValue(key: String,
                              valueClass: Class<V>) =
            internalStore.getValue(key, valueClass)

    override fun <V> getValue(key: String,
                              valueClass: Class<V>,
                              defaultValue: V) =
            internalStore.getValue(key, valueClass, defaultValue)

    override fun <V> saveValue(key: String,
                               value: V?) {
        internalStore.saveValue(key, value)
    }

    override fun hasValue(key: String) = internalStore.hasValue(key)

    override fun deleteValue(key: String) {
        internalStore.deleteValue(key)
    }

    override fun observeChanges() = internalStore.observeChanges()
}
