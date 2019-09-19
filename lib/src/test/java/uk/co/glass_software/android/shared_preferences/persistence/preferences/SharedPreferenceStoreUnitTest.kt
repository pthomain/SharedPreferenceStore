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
import android.content.SharedPreferences.Editor
import com.nhaarman.mockitokotlin2.*
import io.reactivex.subjects.BehaviorSubject
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import uk.co.glass_software.android.boilerplate.core.utils.delegates.Prefs
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser
import uk.co.glass_software.android.shared_preferences.test.verifyNeverWithContext
import uk.co.glass_software.android.shared_preferences.test.verifyWithContext
import java.io.Serializable

class SharedPreferenceStoreUnitTest {

    private lateinit var mockSharedPrefs: Prefs
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockBase64Serialiser: Serialiser
    private lateinit var mockCustomSerialiser: Serialiser
    private lateinit var behaviorSubject: BehaviorSubject<String>
    private lateinit var mockEditor: Editor
    private lateinit var mockLogger: Logger

    private val key = "someKey"
    private val value = "someValue"

    private lateinit var target: SharedPreferenceStore

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mockSharedPrefs = mock()
        mockSharedPreferences = mock()
        mockBase64Serialiser = mock()
        mockCustomSerialiser = mock()
        behaviorSubject = BehaviorSubject.create<String>()

        mockLogger = mock()
        mockEditor = mock()

        whenever(mockSharedPreferences.edit())
                .thenReturn(mockEditor)

        whenever(mockSharedPrefs.file)
                .thenReturn(mockSharedPreferences)

        target = SharedPreferenceStore(
                mockSharedPrefs,
                mockBase64Serialiser,
                mockCustomSerialiser,
                behaviorSubject,
                mockLogger,
                true
        )
    }

    private fun addValue() {
        val map = mapOf(key to value)

        whenever(mockSharedPreferences.all)
                .thenReturn(map)

        whenever(mockSharedPreferences.contains(eq(key)))
                .thenReturn(true)

        whenever(mockSharedPreferences.getString(
                eq(key),
                isNull()
        )).thenReturn(value)

        whenever(mockSharedPrefs.file)
                .thenReturn(mockSharedPreferences)

        //Reset the cache
        target = SharedPreferenceStore(
                mockSharedPrefs,
                mockBase64Serialiser,
                mockCustomSerialiser,
                behaviorSubject,
                mockLogger,
                true
        )
    }

    @Test
    @Throws(Serialiser.SerialisationException::class)
    fun testBase64Read() {
        whenever(
                mockBase64Serialiser.canHandleType(eq(Serializable::class.java))
        ).thenReturn(true)

        addValue()

        target.getValue(key, Serializable::class.java)

        verifyWithContext(mockBase64Serialiser)
                .deserialise(
                        eq(value),
                        eq(Serializable::class.java)
                )
    }

    @Test
    @Throws(Serialiser.SerialisationException::class)
    fun testNotBase64Read() {
        whenever(
                mockBase64Serialiser.canHandleType(eq(Serializable::class.java))
        ).thenReturn(false)

        addValue()

        target.getValue(key, Serializable::class.java)

        verifyNeverWithContext(mockBase64Serialiser)
                .deserialise<Any>(anyString(), any())
    }

    @Test
    fun testDeleteValue() {
        addValue()

        whenever(mockSharedPreferences.contains(eq(key)))
                .thenReturn(true)

        whenever(mockEditor.remove(eq(key)))
                .thenReturn(mockEditor)

        target.deleteValue(key)

        val inOrder = inOrder(mockEditor)

        inOrder.verify(mockEditor).remove(eq(key))
        inOrder.verify(mockEditor).apply()

        val cachedValuesAfter = target.cachedValues
        assertFalse("Cached values should not contain the key", cachedValuesAfter.containsKey(key))
    }

    @Test
    fun testSaveValue() {
        val cachedValues = target.cachedValues
        assertFalse("Cached values should not contain the key", cachedValues.containsKey(key))

        whenever(mockSharedPreferences.contains(eq(key)))
                .thenReturn(false)

        whenever(mockEditor.putString(
                eq(key),
                eq(value)
        )).thenReturn(mockEditor)

        target.saveValue<Any>(key, value)

        val inOrder = inOrder(mockEditor)

        inOrder.verify(mockEditor).putString(
                eq(key),
                eq(value)
        )

        inOrder.verify(mockEditor).apply()

        val cachedValuesAfter = target.cachedValues
        assertTrue("Cached values did not contain the key", cachedValuesAfter.containsKey(key))
        assertEquals("Cached values did not contain the value", value, cachedValuesAfter[key])
    }

    @Test
    fun testGetValue() {
        addValue()

        val value = target.getValue(
                key,
                String::class.java,
                "default"
        )
        assertEquals("Values don't match", this.value, value)
    }

    @Test
    fun testHasValue() {
        addValue()
        assertTrue("Store should have the key", target.hasValue(key))
    }

}