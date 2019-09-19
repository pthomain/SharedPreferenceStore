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

import android.annotation.SuppressLint
import io.reactivex.subjects.Subject
import uk.co.glass_software.android.boilerplate.core.utils.delegates.Prefs
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isBoolean
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isBooleanClass
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isFloat
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isFloatClass
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isInt
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isIntClass
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isLong
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isLongClass
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isString
import uk.co.glass_software.android.shared_preferences.persistence.preferences.TypeUtils.isStringClass
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser
import java.util.*

@Suppress("UNCHECKED_CAST")
internal class SharedPreferenceStore(prefs: Prefs,
                                     private val base64Serialiser: Serialiser,
                                     private val customSerialiser: Serialiser?,
                                     private val changeSubject: Subject<String>,
                                     private val logger: Logger,
                                     private val isMemoryCacheEnabled: Boolean) : KeyValueStore {

    private val sharedPreferences = prefs.file
    private val cacheMap: MutableMap<String, Any> = HashMap()

    val cachedValues: Map<String, Any>
        @Synchronized get() = Collections.unmodifiableMap(cacheMap)

    override fun observeChanges() = changeSubject.map { it }!! // read-only

    @Synchronized
    override fun deleteValue(key: String) {
        saveValue(key, null)
    }

    @Synchronized
    @SuppressLint("CommitPrefEdits")
    override fun <V> saveValue(key: String,
                               value: V?) {
        saveToCache(key, value)

        if (value == null) {
            logger.d(this, "Deleting entry $key")
            sharedPreferences.apply {
                if (contains(key)) {
                    edit().remove(key).apply()
                    if (hasValue(key)) {
                        cacheMap.remove(key)
                    }
                    changeSubject.onNext(key)
                }
            }
        } else {
            try {
                sharedPreferences.edit().apply {
                    when {
                        isBoolean(value) -> putBoolean(key, (value as Boolean))
                        isFloat(value) -> putFloat(key, (value as Float))
                        isLong(value) -> putLong(key, (value as Long))
                        isInt(value) -> putInt(key, (value as Int))
                        isString(value) -> putString(key, value as String)
                        else -> (serialise(value)
                                ?: throw IllegalArgumentException("Value of type ${(value as Any).javaClass.simpleName} cannot be serialised"))
                                .also { putString(key, it) }
                    }
                    apply()
                }

                logger.d(this, "Saving entry $key -> $value")
                changeSubject.onNext(key)
            } catch (e: Exception) {
                logger.e(this, e)
            }
        }
    }

    @Synchronized
    override fun <O> getValue(key: String,
                              valueClass: Class<O>) =
            getNullableValue(key, valueClass, null)

    @Synchronized
    override fun <O> getValue(key: String,
                              valueClass: Class<O>,
                              defaultValue: O) =
            getNullableValue(key, valueClass, defaultValue)!!

    private fun <O> getNullableValue(key: String,
                                     valueClass: Class<O>,
                                     defaultValue: O?): O? =
            cacheMap[key]
                    ?.let { return it as O }
                    ?: getValueInternal(
                            key,
                            valueClass,
                            defaultValue
                    )?.also { saveToCache(key, it) }
                    ?: defaultValue

    @Synchronized
    private fun <O> getValueInternal(key: String,
                                     objectClass: Class<O>,
                                     defaultValue: O?) =
            sharedPreferences.let {
                try {
                    (if (it.contains(key)) {
                        when {
                            isBooleanClass(objectClass) -> it.getBoolean(key, false)
                            isFloatClass(objectClass) -> it.getFloat(key, 0f)
                            isLongClass(objectClass) -> it.getLong(key, 0L)
                            isIntClass(objectClass) -> it.getInt(key, 0)
                            isStringClass(objectClass) -> it.getString(key, null)
                            else -> deserialise(it.getString(key, null), objectClass)
                        } as O? ?: defaultValue
                    } else defaultValue)
                } catch (e: Exception) {
                    logger.e(this, e)
                    defaultValue
                }
            }.also {
                logger.d(this, "Reading entry $key -> $it")
            }

    @Throws(Serialiser.SerialisationException::class)
    private fun serialise(value: Any): String? =
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
            }

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

    @Synchronized
    override fun hasValue(key: String) = sharedPreferences.contains(key)

}
