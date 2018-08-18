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

package uk.co.glass_software.android.shared_preferences.persistence.preferences

import android.content.SharedPreferences
import io.reactivex.subjects.Subject
import uk.co.glass_software.android.boilerplate.log.Logger
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManager
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isBooleanClass
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isFloatClass
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isHandled
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isIntClass
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isLongClass
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isStringClass
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser

@Suppress("UNCHECKED_CAST")
internal class EncryptedSharedPreferenceStore(sharedPreferences: SharedPreferences,
                                              base64Serialiser: Serialiser,
                                              customSerialiser: Serialiser?,
                                              changeSubject: Subject<String>,
                                              logger: Logger,
                                              private val encryptionManager: EncryptionManager?)
    : SharedPreferenceStore(
        sharedPreferences,
        base64Serialiser,
        customSerialiser,
        changeSubject,
        logger
) {

    val isEncryptionSupported by lazy { encryptionManager != null }

    @Synchronized
    override fun saveValueInternal(key: String,
                                   value: Any?) {
        if (value != null && isHandled(value.javaClass))
            super.saveValueInternal(key, encrypt(value.toString(), key))
        else
            super.saveValueInternal(key, value)
    }

    @Synchronized
    override fun <O> getValueInternal(key: String,
                                      objectClass: Class<O>,
                                      defaultValue: O?) =
            if (isHandled(objectClass)) {
                super.getValueInternal(
                        key,
                        String::class.java,
                        null
                )
                        ?.let { decrypt(it, key) }
                        ?.let {
                            when {
                                isBooleanClass(objectClass) -> it.toBoolean()
                                isFloatClass(objectClass) -> it.toFloat()
                                isLongClass(objectClass) -> it.toLong()
                                isIntClass(objectClass) -> it.toInt()
                                isStringClass(objectClass) -> it
                                else -> null
                            } as O?
                        }
            } else {
                super.getValueInternal(key, objectClass, defaultValue)
            }

    override fun serialise(key: String,
                           value: Any) =
            super.serialise(key, value)
                    ?.let { encrypt(it, key) }

    @Throws(Serialiser.SerialisationException::class)
    override fun <O> deserialise(key: String,
                                 serialised: String?,
                                 objectClass: Class<O>) =
            serialised?.let { super.deserialise(key, decrypt(it, key), objectClass) }

    private fun encrypt(clearText: String?,
                        key: String) =
            checkEncryptionAvailable().encrypt(clearText, key)

    private fun decrypt(encrypted: String?,
                        key: String) =
            checkEncryptionAvailable().decrypt(encrypted, key)

    private fun checkEncryptionAvailable() =
            encryptionManager ?: throw IllegalStateException("Encryption is not supported on this device")

}
