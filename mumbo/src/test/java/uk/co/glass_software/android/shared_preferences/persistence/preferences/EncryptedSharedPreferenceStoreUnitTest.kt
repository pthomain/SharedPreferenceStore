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

import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager
import uk.co.glass_software.android.shared_preferences.mumbo.store.EncryptedSharedPreferenceStore
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser

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
        mockBase64Serialiser = mock(Serialiser::class.java)
        mockCustomSerialiser = mock(Serialiser::class.java)
        mockEncryptionManager = mock(EncryptionManager::class.java)
        mockDelegateStore = mock(KeyValueStore::class.java)

        `when`(mockCustomSerialiser.canHandleSerialisedFormat(any())).thenReturn(true)
        `when`(mockCustomSerialiser.canHandleType(any())).thenReturn(true)
        `when`(mockEncryptionManager.isEncryptionSupported).thenReturn(true)

        target = EncryptedSharedPreferenceStore(
                mock(Logger::class.java),
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

        `when`(mockCustomSerialiser.serialise<Any>(eq(value))).thenReturn(serialised)
        `when`(mockEncryptionManager.encrypt(eq(serialised), eq(key))).thenReturn(encryptedValue)

        target.saveValue<Any>(key, value)

        verify(mockDelegateStore).saveValue<Any>(eq(key), eq(encryptedValue))

        val cachedValuesAfter = target.cachedValues
        assertTrue("Cached values did not contain the key", cachedValuesAfter.containsKey(key))
        assertEquals("Cached values did not contain the value", value, cachedValuesAfter[key])
    }

    @Test
    @Throws(Serialiser.SerialisationException::class)
    fun testGetValue() {
        `when`(
                mockDelegateStore.getValue(
                        eq(key),
                        eq(String::class.java)
                )
        ).thenReturn(encryptedValue)

        `when`(
                mockEncryptionManager.decrypt(
                        eq(encryptedValue),
                        eq(key)
                )
        ).thenReturn(serialised)

        `when`(
                mockCustomSerialiser.deserialise(
                        eq(serialised),
                        eq(String::class.java)
                )
        ).thenReturn(value)

        val value = target.getValue(key, String::class.java)

        assertEquals("Values don't match", this.value, value)
    }

}