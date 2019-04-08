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

package uk.co.glass_software.android.shared_preferences.persistence.serialisation

import android.util.Base64
import dagger.Module
import dagger.Provides
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Base64Serialiser.CustomBase64
import javax.inject.Named
import javax.inject.Singleton

@Module
internal class SerialisationModule(private val customSerialiser: Serialiser?) {

    @Provides
    @Singleton
    @Named(BASE_64)
    fun provideBase64Serialiser(logger: Logger): Serialiser = Base64Serialiser(
            logger,
            object : CustomBase64 {
                override fun encode(input: ByteArray, flags: Int) = Base64.encodeToString(input, flags)
                override fun decode(input: String, flags: Int) = Base64.decode(input, flags)
            }
    )

    @Provides
    @Singleton
    @Named(CUSTOM)
    fun provideCustomSerialiser(): Serialiser? = customSerialiser

    companion object {
        const val BASE_64 = "base_64"
        const val CUSTOM = "custom"
    }

}
