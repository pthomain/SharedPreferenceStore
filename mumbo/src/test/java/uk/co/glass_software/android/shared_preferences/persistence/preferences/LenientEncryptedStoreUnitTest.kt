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
import uk.co.glass_software.android.shared_preferences.mumbo.store.LenientEncryptedStore
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore

class LenientEncryptedStoreUnitTest {

    private var mockPlainTextStore: KeyValueStore? = null
    private var mockEncryptedStore: KeyValueStore? = null
    private var mockLogger: Logger? = null

    private var target: LenientEncryptedStore? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mockPlainTextStore = mock(KeyValueStore::class.java)
        mockEncryptedStore = mock(KeyValueStore::class.java)
        mockLogger = mock(Logger::class.java)
    }

    private fun prepareTarget(isEncryptionSupported: Boolean) {
        target = LenientEncryptedStore(
                mockPlainTextStore!!,
                mockEncryptedStore!!,
                isEncryptionSupported,
                mockLogger!!
        )
    }

    private fun reset() {
        Mockito.reset<KeyValueStore>(mockPlainTextStore, mockEncryptedStore)
    }

    @Test
    fun testIsEncryptionSupportedTrue() {
        prepareTarget(true)

        testIsEncryptionSupported(mockEncryptedStore, mockPlainTextStore)
    }

    @Test
    fun testIsEncryptionSupportedFalse() {
        prepareTarget(false)

        testIsEncryptionSupported(mockPlainTextStore, mockEncryptedStore)
    }

    private fun testIsEncryptionSupported(storeToUse: KeyValueStore?,
                                          storeNotToUse: KeyValueStore?) {
        val someKey = "someKey"
        val someValue = "someValue"

        target!!.hasValue(someKey)
        verify<KeyValueStore>(storeToUse).hasValue(eq(someKey))
        verify<KeyValueStore>(storeNotToUse, never()).hasValue(any<String>())
        reset()

        target!!.saveValue<Any>(someKey, someValue)
        verify<KeyValueStore>(storeToUse).saveValue<Any>(eq(someKey), eq(someValue))
        verify<KeyValueStore>(storeNotToUse, never()).saveValue(any<String>(), any<Any>())
        reset()

        target!!.getValue(someKey, String::class.java, someValue)
        verify<KeyValueStore>(storeToUse).getValue(eq(someKey), eq(String::class.java), eq(someValue))
        verify<KeyValueStore>(storeNotToUse, never()).getValue(any<String>(), any<Class<String>>(), any<String>())
        reset()

        target!!.deleteValue(someKey)
        verify<KeyValueStore>(storeToUse).deleteValue(eq(someKey))
        verify<KeyValueStore>(storeNotToUse, never()).deleteValue(any<String>())
    }
}