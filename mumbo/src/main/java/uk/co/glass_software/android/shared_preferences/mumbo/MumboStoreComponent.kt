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

package uk.co.glass_software.android.shared_preferences.mumbo

import dagger.Component
import uk.co.glass_software.android.boilerplate.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [MumboStoreModule::class])
internal interface MumboStoreComponent {

    @Named(PLAIN_TEXT)
    fun store(): KeyValueStore

    @Named(ENCRYPTED)
    fun encryptedStore(): KeyValueStore

    @Named(LENIENT)
    fun lenientStore(): KeyValueStore

    @Named(FORGETFUL)
    fun forgetfulStore(): KeyValueStore

    fun encryptionManager(): EncryptionManager

    fun logger(): Logger

    companion object {
        const val PLAIN_TEXT = "plain_text"
        const val ENCRYPTED = "encrypted"
        const val LENIENT = "lenient"
        const val FORGETFUL = "forgetful"
    }
}