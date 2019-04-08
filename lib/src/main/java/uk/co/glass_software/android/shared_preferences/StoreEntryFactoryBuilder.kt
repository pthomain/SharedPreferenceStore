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
import uk.co.glass_software.android.boilerplate.core.utils.delegates.Prefs
import uk.co.glass_software.android.boilerplate.core.utils.delegates.Prefs.Companion.prefs
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreModule
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreUtils.openSharedPreferences
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.SerialisationModule
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser
import uk.co.glass_software.android.shared_preferences.utils.VoidLogger

class StoreEntryFactoryBuilder internal constructor(private val context: Context) {

    private var prefs: Prefs? = null
    private var logger: Logger? = null
    private var customSerialiser: Serialiser? = null
    private var isMemoryCacheEnabled: Boolean = true

    fun preferences(preferencesFileName: String) = apply {
        this.prefs = context.prefs(preferencesFileName)
    }

    fun logger(logger: Logger) = apply {
        this.logger = logger
    }

    fun customSerialiser(customSerialiser: Serialiser) = apply {
        this.customSerialiser = customSerialiser
    }

    fun setMemoryCacheEnabled(isMemoryCacheEnabled: Boolean) = apply {
        this.isMemoryCacheEnabled = isMemoryCacheEnabled
    }

    fun build(): StoreEntryFactory {
        val component = DaggerSharedPreferenceComponent
                .builder()
                .storeModule(StoreModule(
                        context,
                        prefs ?: openSharedPreferences(context, "shared_preference_store"),
                        logger ?: VoidLogger(),
                        isMemoryCacheEnabled
                ))
                .serialisationModule(SerialisationModule(customSerialiser))
                .build()

        return StoreEntryFactory(component.store())
    }

}
