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

package uk.co.glass_software.android.shared_preferences.persistence.base

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry
import uk.co.glass_software.android.shared_preferences.test.verifyWithContext

class StoreEntryUnitTest {


    private lateinit var target: KeyValueEntry<String>
    private lateinit var mockStore: KeyValueStore

    private inner class TestEntry(store: KeyValueStore) : StoreEntry<String>(
            store,
            KEY,
            String::class.java
    )

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mockStore = mock()
        target = TestEntry(mockStore)
    }

    @Test
    fun testSaveNull() {
        target.save(null)

        verifyWithContext(mockStore).saveValue<Any>(
                eq(KEY),
                isNull()
        )
    }

    @Test
    fun testSave() {
        val value = "some string"

        target.save(value)

        verifyWithContext(mockStore).saveValue(
                eq(KEY),
                eq(value)
        )
    }

    @Test
    fun testGet() {
        val test = "test"

        whenever(mockStore.getValue(
                eq(KEY),
                eq(String::class.java)
        )).thenReturn(test)

        val result = target.get()

        verifyWithContext(mockStore).getValue(
                eq(KEY),
                eq(String::class.java)
        )

        assertEquals("Strings don't match", test, result)
    }

    @Test
    fun testGetWithDefault() {
        val string = "abc"
        val defaultValue = "default"

        whenever(mockStore.getValue(
                eq(KEY),
                eq(String::class.java),
                eq(defaultValue)
        )).thenReturn(string)

        val result = target.get(defaultValue)

        verifyWithContext(mockStore).getValue(
                eq(KEY),
                eq(String::class.java),
                eq(defaultValue)
        )

        assertEquals("Strings don't match", string, result)
    }

    @Test
    fun testDrop() {
        target.drop()

        verifyWithContext(mockStore)
                .deleteValue(eq(KEY))
    }

    @Test
    fun testGetKey() {
        val key = target.getKey()

        assertEquals("Key is wrong", KEY, key)
    }

    @Test
    @Throws(Exception::class)
    fun testExists() {
        whenever(
                mockStore.hasValue(eq(KEY))
        ).thenReturn(true)

        assertTrue("Entry should exists", target.exists())

        whenever(
                mockStore.hasValue(eq(KEY))
        ).thenReturn(false)

        assertFalse("Entry should exists", target.exists())

        verifyWithContext(mockStore).hasValue(eq(KEY))
    }

    companion object {
        private const val KEY = "KEY"
    }
}