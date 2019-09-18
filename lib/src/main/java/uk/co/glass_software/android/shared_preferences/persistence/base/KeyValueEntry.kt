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

package uk.co.glass_software.android.shared_preferences.persistence.base

import io.reactivex.Observable
import uk.co.glass_software.android.boilerplate.core.utils.optional.Optional
import uk.co.glass_software.android.boilerplate.core.utils.rx.On

interface KeyValueEntry<C> {

    fun getKey(): String

    fun save(value: C?)

    fun get(): C?

    fun get(defaultValue: C): C

    fun <S : C> getAs(subclass: Class<S>): S?

    fun <S : C> getAs(subclass: Class<S>,
                      defaultValue: S): S

    fun maybe(): Optional<C>

    fun drop()

    fun exists(): Boolean

    fun observe(emitCurrentValue: Boolean = false,
                observeOn: On = On.MainThread): Observable<Optional<C>>

}

interface KeyClassProvider<C> : UniqueKeyProvider, ValueClassProvider<C>

interface OptionalEntry<C> : KeyValueEntry<C>, Optional<C>

interface UniqueKeyProvider {
    val uniqueKey: String
}

interface ValueClassProvider<C> {
    val valueClass: Class<C>
}