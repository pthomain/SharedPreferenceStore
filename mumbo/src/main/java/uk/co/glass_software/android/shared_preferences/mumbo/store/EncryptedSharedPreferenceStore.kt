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
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser
import java.util.*

@Suppress("UNCHECKED_CAST")
internal class EncryptedSharedPreferenceStore(private val logger: Logger,
                                              private val base64Serialiser: Serialiser,
                                              private val customSerialiser: Serialiser?,
                                              private val plainTextStore: KeyValueStore,
                                              private val encryptionManager: EncryptionManager,
                                              private val isMemoryCacheEnabled: Boolean)
    : KeyValueStore by plainTextStore {

    private val cacheMap: MutableMap<String, Any> = HashMap()

    val cachedValues: Map<String, Any>
        @Synchronized get() = Collections.unmodifiableMap(cacheMap)

    override fun <V> saveValue(key: String,
                               value: V?) {
        saveToCache(key, value)

        plainTextStore.saveValue(
                key,
                value?.let { serialise(key, it as Any) }
        )
    }

    override fun <V> getValue(key: String,
                              valueClass: Class<V>): V? =
            plainTextStore.getValue(
                    key,
                    String::class.java
            )?.let { getValueInternal(it, key, valueClass) }

    override fun <V> getValue(key: String,
                              valueClass: Class<V>,
                              defaultValue: V): V =
            getValue(key, valueClass) ?: defaultValue

    private fun <V> getValueInternal(encrypted: String,
                                     key: String,
                                     valueClass: Class<V>): V? =
            decrypt(encrypted, key)?.let {
                deserialise(it, valueClass)
            }.also { saveToCache(key, it) }

    @Throws(Serialiser.SerialisationException::class)
    private fun serialise(key: String,
                          value: Any) =
            value.javaClass.let {
                try {
                    when {
                        customSerialiser?.canHandleType(it) == true -> customSerialiser.serialise(value)
                        base64Serialiser.canHandleType(it) -> base64Serialiser.serialise(value)
                        else -> null
                    }
                } catch (e: Serialiser.SerialisationException) {
                    logger.e(this, e, "Could not serialise $value")
                    null
                }
            }.let { encrypt(it, key) }

    @Throws(Serialiser.SerialisationException::class)
    private fun <O> deserialise(serialised: String?,
                                objectClass: Class<O>) =
            serialised?.let {
                when {
                    customSerialiser?.canHandleType(objectClass) == true -> customSerialiser.deserialise(serialised, objectClass)
                    base64Serialiser.canHandleType(objectClass) -> base64Serialiser.deserialise(serialised, objectClass)
                    else -> null
                }
            }

    private fun encrypt(clearText: String?,
                        key: String) =
            checkEncryptionAvailable().encrypt(clearText, key)

    private fun decrypt(encrypted: String?,
                        key: String) =
            checkEncryptionAvailable().decrypt(encrypted, key)

    private fun checkEncryptionAvailable() =
            if (!encryptionManager.isEncryptionSupported)
                throw IllegalStateException("Encryption is not supported on this device")
            else encryptionManager

    @Synchronized
    private fun saveToCache(key: String,
                            value: Any?) {
        if (isMemoryCacheEnabled) {
            if (value == null)
                cacheMap.remove(key)
            else
                cacheMap[key] = value
        }
    }

}
