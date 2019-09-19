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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager
import uk.co.glass_software.android.shared_preferences.mumbo.store.EncryptedSharedPreferenceStore
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser
import uk.co.glass_software.android.shared_preferences.test.verifyWithContext

class EncryptedSharedPreferenceStoreUnitTest {

    private lateinit var mockBase64Serialiser: Serialiser
    private lateinit var mockCustomSerialiser: Serialiser
    private lateinit var mockEncryptionManager: EncryptionManager
    private lateinit var mockDelegateStore: KeyValueStore

    private val key = "key"
    private val value = "value"
    private val serialised = "serialised"
    private val encryptedValue = "encryptedValue"

    private lateinit var target: EncryptedSharedPreferenceStore

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mockBase64Serialiser = mock()
        mockCustomSerialiser = mock()
        mockEncryptionManager = mock()
        mockDelegateStore = mock()

        whenever(mockCustomSerialiser.canHandleSerialisedFormat(any())).thenReturn(true)
        whenever(mockCustomSerialiser.canHandleType(any())).thenReturn(true)
        whenever(mockEncryptionManager.isEncryptionSupported).thenReturn(true)

        target = EncryptedSharedPreferenceStore(
                mock(),
                mockBase64Serialiser,
                mockCustomSerialiser,
                mockDelegateStore,
                mockEncryptionManager,
                true
        )
    }

    @Test
    @Throws(Serialiser.SerialisationException::class)
    fun testSaveValue() {
        val cachedValues = target.cachedValues
        assertFalse("Cached values should not contain the key", cachedValues.containsKey(key))

        whenever(mockCustomSerialiser.serialise<Any>(eq(value))).thenReturn(serialised)
        whenever(mockEncryptionManager.encrypt(eq(serialised), eq(key))).thenReturn(encryptedValue)

        target.saveValue<Any>(key, value)

        verifyWithContext(mockDelegateStore).saveValue(eq(key), eq(encryptedValue))

        val cachedValuesAfter = target.cachedValues
        assertTrue("Cached values did not contain the key", cachedValuesAfter.containsKey(key))
        assertEquals("Cached values did not contain the value", value, cachedValuesAfter[key])
    }

    @Test
    @Throws(Serialiser.SerialisationException::class)
    fun testGetValue() {
        whenever(
                mockDelegateStore.getValue(
                        eq(key),
                        eq(String::class.java)
                )
        ).thenReturn(encryptedValue)

        whenever(
                mockEncryptionManager.decrypt(
                        eq(encryptedValue),
                        eq(key)
                )
        ).thenReturn(serialised)

        whenever(
                mockCustomSerialiser.deserialise(
                        eq(serialised),
                        eq(String::class.java)
                )
        ).thenReturn(value)

        val value = target.getValue(key, String::class.java)

        assertEquals("Values don't match", this.value, value)
    }

}