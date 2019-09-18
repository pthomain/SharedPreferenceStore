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

import io.reactivex.Observable
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import uk.co.glass_software.android.boilerplate.core.utils.rx.On
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore

class StoreEntryTest {

    private val someValue = "someValue"

    private lateinit var mockStore: KeyValueStore

    private lateinit var target: KeyValueEntry<String>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mockStore = mock(KeyValueStore::class.java)
        target = StoreEntry(
                mockStore,
                KEY,
                String::class.java
        )
    }

    @Test
    fun testSave() {
        target.save(someValue)

        verify<KeyValueStore>(mockStore).saveValue(
                eq(KEY),
                eq(someValue)
        )
    }

    @Test
    fun testGet() {
        `when`<String>(
                mockStore.getValue(
                        eq(KEY),
                        eq(String::class.java)
                )
        ).thenReturn(someValue)

        assertEquals(someValue, target.get())
    }

    @Test
    fun testGetWithDefaultValue() {
        val defaultValue = "defaultValue"

        `when`(
                mockStore.getValue(
                        eq(KEY),
                        eq(String::class.java),
                        eq(defaultValue)
                )
        ).thenReturn(someValue)

        assertEquals(someValue, target.get(defaultValue))
    }

    @Test
    fun testDrop() {
        target.drop()

        verify(mockStore).deleteValue(eq(KEY))
    }

    @Test
    fun testGetKey() {
        assertEquals(KEY, target.getKey())
    }

    @Test
    fun testExistsTrue() {
        `when`(
                mockStore.hasValue(eq(KEY))
        ).thenReturn(true)

        assertTrue(target.exists())
    }

    @Test
    fun testExistsFalse() {
        `when`(
                mockStore.hasValue(eq(KEY))
        ).thenReturn(false)

        assertFalse(target.exists())
    }

    @Test
    fun testMaybeEmpty() {
        `when`(
                mockStore.hasValue(eq(KEY))
        ).thenReturn(false)

        assertFalse(target.maybe().isPresent())
    }

    @Test
    fun testMaybe() {
        `when`(mockStore.hasValue(eq(KEY)))
                .thenReturn(true)

        `when`(
                mockStore.getValue(
                        eq(KEY),
                        eq(String::class.java)
                )
        ).thenReturn(someValue)

        assertEquals(someValue, target.maybe().get())
    }

    @Test
    fun testObserveWithWrongKey() {
        `when`(mockStore.observeChanges())
                .thenReturn(Observable.just("wrongKey"))

        target.observe(false, On.Trampoline).subscribe()

        verify(mockStore, never())
                .getValue(anyString(), any<Class<*>>())
    }

    @Test
    fun testObserve() {
        `when`(mockStore.observeChanges())
                .thenReturn(Observable.just(KEY))

        `when`(mockStore.hasValue(eq(KEY)))
                .thenReturn(true)

        `when`(mockStore.getValue(
                eq(KEY),
                eq(String::class.java)
        )).thenReturn(someValue)

        assertTrue(target.observe(false, On.Trampoline).blockingFirst().isPresent())

        assertEquals(
                someValue,
                target.observe(false, On.Trampoline).blockingFirst().get()
        )
    }

    companion object {
        const val KEY = "KEY"
    }
}