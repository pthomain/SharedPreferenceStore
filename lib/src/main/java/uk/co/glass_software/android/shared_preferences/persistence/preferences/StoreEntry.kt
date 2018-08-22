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

import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class StoreEntry<C> @JvmOverloads constructor(private val store: KeyValueStore,
                                                   keyProvider: UniqueKeyProvider,
                                                   valueClassProvider: ValueClassProvider,
                                                   private val defaultValue: C? = null)
    : KeyValueEntry<C> {

    private val valueClass: Class<C>
    private val keyString: String

    @JvmOverloads
    constructor(store: KeyValueStore,
                key: String,
                valueClass: Class<C>,
                defaultValue: C? = null)
            : this(
            store,
            { key } as UniqueKeyProvider,
            { valueClass } as ValueClassProvider,
            defaultValue
    )

    @JvmOverloads
    constructor(store: KeyValueStore,
                keyProvider: KeyClassProvider,
                defaultValue: C? = null)
            : this(store, keyProvider, keyProvider, defaultValue)

    init {
        this.keyString = keyProvider.uniqueKey
        this.valueClass = valueClassProvider.valueClass as Class<C>
    }

    @Synchronized
    override fun save(value: C?) {
        store.saveValue(getKey(), value)
    }

    @Synchronized
    override fun get(): C? = get(defaultValue)

    @Synchronized
    override operator fun get(defaultValue: C?): C? {
        return store.getValue(getKey(), valueClass, defaultValue)
    }

    @Synchronized
    override fun drop() {
        store.deleteValue(getKey())
    }

    override fun getKey(): String {
        return keyString
    }

    override fun exists(): Boolean {
        return store.hasValue(getKey())
    }

    interface KeyClassProvider : UniqueKeyProvider, ValueClassProvider

    interface UniqueKeyProvider {
        val uniqueKey: String
    }

    interface ValueClassProvider {
        val valueClass: Class<*>
    }

    companion object {

        inline infix fun <reified T> StoreEntry<T>.delegatedDefault(defaultValue: T?) =
                StoreEntryDelegate(this, defaultValue, true)

        inline fun <reified T> StoreEntry<T>.delegated() =
                StoreEntryDelegate(this, null, false)

        class StoreEntryDelegate<T>(private val entry: KeyValueEntry<T>,
                                    private val defaultValue: T? = null,
                                    private val useDefaultValue: Boolean)
            : ReadWriteProperty<Any, T?> {

            override operator fun getValue(thisRef: Any, property: KProperty<*>): T? =
                    if (useDefaultValue) entry.get(defaultValue) else entry.get()

            override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
                entry.save(value)
            }
        }
    }
}

