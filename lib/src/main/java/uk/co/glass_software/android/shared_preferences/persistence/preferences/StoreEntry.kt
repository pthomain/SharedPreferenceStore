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

import uk.co.glass_software.android.boilerplate.core.utils.optional.Optional
import uk.co.glass_software.android.boilerplate.core.utils.optional.OptionalValue
import uk.co.glass_software.android.boilerplate.core.utils.rx.On
import uk.co.glass_software.android.shared_preferences.persistence.base.*

open class StoreEntry<C> @JvmOverloads constructor(private val store: KeyValueStore,
                                                   keyProvider: UniqueKeyProvider,
                                                   valueClassProvider: ValueClassProvider<C>,
                                                   private val defaultValue: C? = null)
    : OptionalEntry<C>,
        UniqueKeyProvider by keyProvider,
        ValueClassProvider<C> by valueClassProvider {

    @JvmOverloads
    constructor(store: KeyValueStore,
                keyProvider: KeyClassProvider<C>,
                defaultValue: C? = null)
            : this(store, keyProvider, keyProvider, defaultValue)

    @JvmOverloads
    constructor(store: KeyValueStore,
                key: String,
                valueClass: Class<C>,
                defaultValue: C? = null)
            : this(
            store,
            object : UniqueKeyProvider {
                override val uniqueKey = key
            },
            object : ValueClassProvider<C> {
                override val valueClass = valueClass
            },
            defaultValue
    )

    @Synchronized
    override fun get() =
            store.getValue(getKey(), valueClass) ?: defaultValue

    @Synchronized
    override operator fun get(defaultValue: C): C =
            store.getValue(getKey(), valueClass, defaultValue)

    @Synchronized
    override fun <S : C> getAs(subclass: Class<S>) =
            store.getValue(getKey(), subclass)

    @Synchronized
    override fun <S : C> getAs(subclass: Class<S>, defaultValue: S) =
            store.getValue(uniqueKey, subclass, defaultValue)

    @Synchronized
    override fun maybe() =
            OptionalValue.ofNullable(get())

    @Synchronized
    override fun save(value: C?) {
        store.saveValue(getKey(), value)
    }

    @Synchronized
    override fun drop() {
        store.deleteValue(getKey())
    }

    override fun getKey() = uniqueKey

    override fun exists() = store.hasValue(getKey())

    override fun observe(emitCurrentValue: Boolean,
                         observeOn: On) =
            store.observeChanges()
                    .filter { it == getKey() }
                    .map { maybe() }
                    .compose { if (emitCurrentValue) it.startWith(maybe()) else it }
                    .observeOn(observeOn())!!

    override fun isPresent() = exists()

    override fun ifPresent(consumer: (C) -> Unit) {
        maybe().ifPresent(consumer)
    }

    override fun filter(predicate: (C) -> Boolean) =
            maybe().filter(predicate)

    override fun <U> map(mapper: (C) -> U) =
            maybe().map(mapper)

    override fun <U> flatMap(mapper: (C) -> Optional<U>) =
            maybe().flatMap(mapper)

    override fun orElse(other: C) =
            maybe().orElse(other)

    override fun orElseGet(other: () -> C) =
            maybe().orElseGet(other)

    override fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X) =
            maybe().orElseThrow(exceptionSupplier)

}

