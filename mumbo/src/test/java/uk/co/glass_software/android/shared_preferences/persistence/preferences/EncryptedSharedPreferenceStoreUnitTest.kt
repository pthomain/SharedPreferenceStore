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

import android.content.SharedPreferences
import io.reactivex.subjects.BehaviorSubject
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.`when`
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import uk.co.glass_software.android.boilerplate.utils.log.Logger
import uk.co.glass_software.android.boilerplate.utils.preferences.Prefs
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager
import uk.co.glass_software.android.shared_preferences.mumbo.store.EncryptedSharedPreferenceStore
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser

class EncryptedSharedPreferenceStoreUnitTest {

    private var mockSharedPrefs: Prefs? = null
    private var mockSharedPreferences: SharedPreferences? = null
    private var mockBase64Serialiser: Serialiser? = null
    private var mockCustomSerialiser: Serialiser? = null
    private var behaviorSubject: BehaviorSubject<String>? = null
    private var mockEditor: SharedPreferences.Editor? = null
    private var mockEncryptionManager: EncryptionManager? = null
    private var mockLogger: Logger? = null

    private val key = "key"
    private val value = "value"
    private val encryptedValue = "encryptedValue"

    private var target: EncryptedSharedPreferenceStore? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mockSharedPreferences = mock(SharedPreferences::class.java)
        mockSharedPrefs = mock(Prefs::class.java)
        mockBase64Serialiser = mock(Serialiser::class.java)
        mockCustomSerialiser = mock(Serialiser::class.java)
        behaviorSubject = BehaviorSubject.create()
        mockLogger = mock(Logger::class.java)
        mockEncryptionManager = mock(EncryptionManager::class.java)

        `when`(mockCustomSerialiser!!.canHandleSerialisedFormat(any<String>())).thenReturn(true)
        `when`(mockCustomSerialiser!!.canHandleType(any<Class<*>>())).thenReturn(true)

        mockEditor = mock(SharedPreferences.Editor::class.java)
        `when`<SharedPreferences.Editor>(mockSharedPreferences!!.edit()).thenReturn(mockEditor)
        `when`(mockSharedPrefs!!.file).thenReturn(mockSharedPreferences)

        target = EncryptedSharedPreferenceStore(
                mockSharedPrefs!!,
                mockBase64Serialiser!!,
                mockCustomSerialiser,
                behaviorSubject!!,
                mockLogger!!,
                mockEncryptionManager!!
        )

        `when`<String>(mockEncryptionManager!!.decrypt(eq(encryptedValue), eq(key))).thenReturn(value)
        `when`<String>(mockEncryptionManager!!.encrypt(eq(value), eq(key))).thenReturn(encryptedValue)

    }

    @Test
    @Throws(Serialiser.SerialisationException::class)
    fun testSaveValue() {
        val cachedValues = target!!.cachedValues
        assertFalse("Cached values should not contain the key", cachedValues.containsKey(key))

        `when`(mockSharedPreferences!!.contains(eq(key))).thenReturn(false)
        `when`<SharedPreferences.Editor>(mockEditor!!.putString(eq(key), eq(encryptedValue))).thenReturn(mockEditor)
        `when`(mockCustomSerialiser!!.serialise<Any>(eq(value))).thenReturn(value)

        target!!.saveValue<Any>(key, value)

        verify<Serialiser>(mockCustomSerialiser).serialise<Any>(eq(value))

        val inOrder = inOrder(mockEditor)

        inOrder.verify<SharedPreferences.Editor>(mockEditor).putString(eq(key), eq(encryptedValue))
        inOrder.verify<SharedPreferences.Editor>(mockEditor).apply()

        val cachedValuesAfter = target!!.cachedValues
        assertTrue("Cached values did not contain the key", cachedValuesAfter.containsKey(key))
        assertEquals("Cached values did not contain the value", value, cachedValuesAfter[key])

        assertEquals(key, behaviorSubject!!.blockingFirst())
    }

    @Test
    @Throws(Serialiser.SerialisationException::class)
    fun testGetValue() {
        `when`(mockSharedPreferences!!.contains(eq(key))).thenReturn(true)
        `when`(mockSharedPreferences!!.getString(eq(key), isNull<String>())).thenReturn(encryptedValue)
        `when`(mockCustomSerialiser!!.deserialise(eq(value), eq(String::class.java))).thenReturn(value)

        val value = target!!.getValue(key, String::class.java)

        assertEquals("Values don't match", this.value, value)
    }

}