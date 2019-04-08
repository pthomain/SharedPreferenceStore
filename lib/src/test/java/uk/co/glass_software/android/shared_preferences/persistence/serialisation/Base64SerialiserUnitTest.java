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

package uk.co.glass_software.android.shared_preferences.persistence.serialisation;

import android.util.Base64;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import uk.co.glass_software.android.boilerplate.core.utils.log.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.StoreEntry;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Base64SerialiserUnitTest {
    
    private Base64Serialiser.CustomBase64 mockBase64;
    
    private Base64Serialiser target;
    
    @Before
    public void setUp() throws Exception {
        mockBase64 = mock(Base64Serialiser.CustomBase64.class);
        target = new Base64Serialiser(
                mock(Logger.class),
                mockBase64
        );
    }
    
    @Test
    public void testCanHandleType() {
        assertTrue(target.canHandleType(Serializable.class));
        assertTrue(target.canHandleType(String.class));
        assertTrue(target.canHandleType(Date.class));
        assertTrue(target.canHandleType(Integer.class));
        assertFalse(target.canHandleType(StoreEntry.class));
        assertFalse(target.canHandleType(Logger.class));
    }
    
    @Test
    public void testCanHandleSerialisedFormat() {
        assertFalse(target.canHandleSerialisedFormat(""));
        assertFalse(target.canHandleSerialisedFormat("someString"));
        assertFalse(target.canHandleSerialisedFormat("BASE_64_someString"));
        assertFalse(target.canHandleSerialisedFormat("abcdBASE_64_someString_START_DATA_"));
        assertTrue(target.canHandleSerialisedFormat("BASE_64_someString_START_DATA_"));
        assertTrue(target.canHandleSerialisedFormat("BASE_64_someString_START_DATA_abcd"));
    }
    
    @Test
    public void testSerialise() throws Exception {
        String originalValue = "originalValue";
        String base64 = "base64";
        
        byte[] bytes = getBytes(originalValue);
        
        when(mockBase64.encode(eq(bytes), eq(Base64.DEFAULT))).thenReturn(base64);
        
        assertEquals("BASE_64_java.lang.String_START_DATA_base64", target.serialise(originalValue));
    }
    
    @Test
    public void testDeserialise() throws Exception {
        String originalValue = "originalValue";
        String base64 = "base64";
        byte[] objectBytes = getBytes(originalValue);
        
        when(mockBase64.decode(eq(base64), eq(Base64.DEFAULT))).thenReturn(objectBytes);
        
        assertEquals(originalValue, target.deserialise("BASE_64_java.lang.String_START_DATA_base64", String.class));
    }
    
    @Test(expected = Serialiser.SerialisationException.class)
    public void testDeserialiseWrongClass() throws Exception {
        target.deserialise("BASE_64_java.lang.Integer_START_DATA_base64", String.class);
    }
    
    private byte[] getBytes(String originalValue) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(originalValue);
        return bos.toByteArray();
    }
    
}