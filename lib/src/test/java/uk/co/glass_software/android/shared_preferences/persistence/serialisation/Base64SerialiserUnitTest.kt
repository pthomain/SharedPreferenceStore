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

package uk.co.glass_software.android.shared_preferences.persistence.serialisation

import android.util.Base64
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import uk.co.glass_software.android.boilerplate.core.utils.log.Logger
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Base64Serialiser.CustomBase64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

class Base64SerialiserUnitTest {

    private lateinit var mockBase64: CustomBase64

    private lateinit var target: Base64Serialiser

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mockBase64 = mock()

        target = Base64Serialiser(
                mock(),
                mockBase64
        )
    }

    @Test
    fun testCanHandleType() {
        assertTrue(target.canHandleType(Serializable::class.java))
        assertTrue(target.canHandleType(String::class.java))
        assertTrue(target.canHandleType(Date::class.java))
        assertTrue(target.canHandleType(Int::class.java))
        assertFalse(target.canHandleType(StoreEntry::class.java))
        assertFalse(target.canHandleType(Logger::class.java))
    }

    @Test
    fun testCanHandleSerialisedFormat() {
        assertFalse(target.canHandleSerialisedFormat(""))
        assertFalse(target.canHandleSerialisedFormat("someString"))
        assertFalse(target.canHandleSerialisedFormat("BASE_64_someString"))
        assertFalse(target.canHandleSerialisedFormat("abcdBASE_64_someString_START_DATA_"))
        assertTrue(target.canHandleSerialisedFormat("BASE_64_someString_START_DATA_"))
        assertTrue(target.canHandleSerialisedFormat("BASE_64_someString_START_DATA_abcd"))
    }

    @Test
    @Throws(Exception::class)
    fun testSerialise() {
        val originalValue = "originalValue"
        val base64 = "base64"

        val bytes = getBytes(originalValue)

        whenever(
                mockBase64.encode(
                        eq(bytes),
                        eq(Base64.DEFAULT)
                )
        ).thenReturn(base64)

        assertEquals(
                "BASE_64_java.lang.String_START_DATA_base64",
                target.serialise(originalValue)
        )
    }

    @Test
    @Throws(Exception::class)
    fun testDeserialise() {
        val originalValue = "originalValue"
        val base64 = "base64"
        val objectBytes = getBytes(originalValue)

        whenever(
                mockBase64.decode(
                        eq(base64),
                        eq(Base64.DEFAULT)
                )
        ).thenReturn(objectBytes)

        assertEquals(
                originalValue,
                target.deserialise("BASE_64_java.lang.String_START_DATA_base64", String::class.java)
        )
    }

    @Test(expected = Serialiser.SerialisationException::class)
    @Throws(Exception::class)
    fun testDeserialiseWrongClass() {
        target.deserialise("BASE_64_java.lang.Integer_START_DATA_base64", String::class.java)
    }

    @Throws(IOException::class)
    private fun getBytes(originalValue: String): ByteArray {
        val bos = ByteArrayOutputStream()
        val out = ObjectOutputStream(bos)
        out.writeObject(originalValue)
        return bos.toByteArray()
    }

}