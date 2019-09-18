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
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.mumbo.store.ForgetfulEncryptedStore
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore

class ForgetfulEncryptedStoreUnitTest {

    private lateinit var mockEncryptedStore: KeyValueStore
    private lateinit var mockLogger: Logger

    private lateinit var target: ForgetfulEncryptedStore

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mockEncryptedStore = mock(KeyValueStore::class.java)
        mockLogger = mock(Logger::class.java)
    }

    private fun prepareTarget(isEncryptionSupported: Boolean) {
        target = ForgetfulEncryptedStore(
                mockEncryptedStore,
                isEncryptionSupported,
                mockLogger
        )
    }

    private fun reset() {
        Mockito.reset(mockEncryptedStore)
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

        target.hasValue(someKey)

        if (shouldUseInternalStore) {
            verify(mockEncryptedStore).hasValue(eq(someKey))
        } else {
            verify(mockEncryptedStore, never()).hasValue(any())
        }

        reset()

        target.saveValue<Any>(someKey, someValue)

        if (shouldUseInternalStore) {
            verify(mockEncryptedStore)
                    .saveValue(
                            eq(someKey),
                            eq(someValue)
                    )
        } else {
            verify(mockEncryptedStore, never())
                    .saveValue(
                            anyString(),
                            any<Any>()
                    )
        }

        reset()

        target.getValue(someKey, String::class.java, someValue)

        if (shouldUseInternalStore) {
            verify(mockEncryptedStore)
                    .getValue(
                            eq(someKey),
                            eq(String::class.java),
                            eq(someValue)
                    )
        } else {
            verify(mockEncryptedStore, never())
                    .getValue(
                            anyString(),
                            any(),
                            any<Any>()
                    )
        }

        reset()

        target.deleteValue(someKey)

        if (shouldUseInternalStore) {
            verify(mockEncryptedStore).deleteValue(eq(someKey))
        } else {
            verify(mockEncryptedStore, never()).deleteValue(any())
        }
    }
}