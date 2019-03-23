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

package uk.co.glass_software.android.shared_preferences.utils

import uk.co.glass_software.android.shared_preferences.StoreEntryFactory
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyClassProvider
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SimpleEntry<T>(entryFactory: StoreEntryFactory,
                     override val uniqueKey: String,
                     override val valueClass: Class<T>)
    : KeyValueEntry<T> by entryFactory.open(uniqueKey, valueClass),
        KeyClassProvider<T>

inline fun <reified T> StoreEntryFactory.pref(uniqueKey: String) =
        SimpleEntry(
                this,
                uniqueKey,
                T::class.java
        )

inline infix fun <reified T> KeyValueEntry<T>.delegatedDefault(defaultValue: T?) =
        StoreEntryDelegate(this, defaultValue, true)

inline fun <reified T> KeyValueEntry<T>.delegated() =
        StoreEntryDelegate(this, null, false)

class StoreEntryDelegate<T>(private val entry: KeyValueEntry<T>,
                            private val defaultValue: T? = null,
                            private val useDefaultValue: Boolean)
    : ReadWriteProperty<Any, T?> {

    override operator fun getValue(thisRef: Any,
                                   property: KProperty<*>): T? =
            if (useDefaultValue && defaultValue != null) entry.get(defaultValue) else entry.get()

    override operator fun setValue(thisRef: Any,
                                   property: KProperty<*>, value: T?) {
        entry.save(value)
    }
}