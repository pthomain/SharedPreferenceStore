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

import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.subjects.PublishSubject
import uk.co.glass_software.android.boilerplate.core.utils.delegates.Prefs
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule.Companion.BASE_64
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule.Companion.CUSTOM
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [SerialisationModule::class])
internal class StoreModule(private val context: Context,
                           private val prefs: Prefs,
                           private val logger: Logger,
                           private val isMemoryCacheEnabled: Boolean) {

    @Provides
    @Singleton
    fun provideContext() = context

    @Provides
    @Singleton
    fun provideSharedPreferenceStore(@Named(BASE_64) base64Serialiser: Serialiser,
                                     @Named(CUSTOM) customSerialiser: Serialiser?,
                                     logger: Logger): SharedPreferenceStore =
            SharedPreferenceStore(
                    prefs,
                    base64Serialiser,
                    customSerialiser,
                    PublishSubject.create(),
                    logger,
                    isMemoryCacheEnabled
            )


    @Provides
    @Singleton
    fun provideStore(store: SharedPreferenceStore): KeyValueStore = store

    @Provides
    @Singleton
    fun provideLogger() = logger

    companion object {
        internal const val MAX_FILE_NAME_LENGTH = 127
    }

}
