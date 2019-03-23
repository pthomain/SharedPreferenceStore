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

import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.*
import uk.co.glass_software.android.boilerplate.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.mumbo.store.ForgetfulEncryptedStore
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore

class ForgetfulEncryptedStoreUnitTest {

    private var mockEncryptedStore: KeyValueStore? = null
    private var mockLogger: Logger? = null

    private var target: ForgetfulEncryptedStore? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mockEncryptedStore = mock(KeyValueStore::class.java)
        mockLogger = mock(Logger::class.java)
    }

    private fun prepareTarget(isEncryptionSupported: Boolean) {
        target = ForgetfulEncryptedStore(
                mockEncryptedStore!!,
                isEncryptionSupported,
                mockLogger!!
        )
    }

    private fun reset() {
        Mockito.reset<KeyValueStore>(mockEncryptedStore)
    }

    @Test
    fun testIsEncryptionSupportedTrue() {
        prepareTarget(true)

        testIsEncryptionSupported(true)
    }

    @Test
    fun testIsEncryptionSupportedFalse() {
        prepareTarget(false)

        testIsEncryptionSupported(false)
    }

    private fun testIsEncryptionSupported(shouldUseInternalStore: Boolean) {
        val someKey = "someKey"
        val someValue = "someValue"

        target!!.hasValue(someKey)
        if (shouldUseInternalStore) {
            verify<KeyValueStore>(mockEncryptedStore).hasValue(eq(someKey))
        } else {
            verify<KeyValueStore>(mockEncryptedStore, never()).hasValue(any<String>())
        }
        reset()

        target!!.saveValue<Any>(someKey, someValue)
        if (shouldUseInternalStore) {
            verify<KeyValueStore>(mockEncryptedStore).saveValue<Any>(eq(someKey), eq(someValue))
        } else {
            verify<KeyValueStore>(mockEncryptedStore, never()).saveValue(any<String>(), any<Any>())
        }
        reset()

        target!!.getValue(someKey, String::class.java, someValue)
        if (shouldUseInternalStore) {
            verify<KeyValueStore>(mockEncryptedStore).getValue(eq(someKey), eq(String::class.java), eq(someValue))
        } else {
            verify<KeyValueStore>(mockEncryptedStore, never()).getValue(any<String>(), any<Class<String>>(), any<String>())
        }
        reset()

        target!!.deleteValue(someKey)
        if (shouldUseInternalStore) {
            verify<KeyValueStore>(mockEncryptedStore).deleteValue(eq(someKey))
        } else {
            verify<KeyValueStore>(mockEncryptedStore, never()).deleteValue(any<String>())
        }
    }
}