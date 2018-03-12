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

package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Map;

import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.utils.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManager;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncryptedSharedPreferenceStoreUnitTest {
    
    private SharedPreferences mockSharedPreferences;
    private Serialiser mockBase64Serialiser;
    private Serialiser mockCustomSerialiser;
    private BehaviorSubject behaviorSubject;
    private SharedPreferences.Editor mockEditor;
    private EncryptionManager mockEncryptionManager;
    private Logger mockLogger;
    
    private final String key = "key";
    private final String value = "value";
    private final String encryptedValue = "encryptedValue";
    
    private EncryptedSharedPreferenceStore target;
    private String dataTag = "dataTag";
    
    @Before
    public void setUp() throws Exception {
        mockSharedPreferences = mock(SharedPreferences.class);
        mockBase64Serialiser = mock(Serialiser.class);
        mockCustomSerialiser = mock(Serialiser.class);
        behaviorSubject = BehaviorSubject.create();
        mockLogger = mock(Logger.class);
        mockEncryptionManager = mock(EncryptionManager.class);
        
        mockEditor = mock(SharedPreferences.Editor.class);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        
        target = new EncryptedSharedPreferenceStore(
                mockSharedPreferences,
                mockBase64Serialiser,
                mockCustomSerialiser,
                behaviorSubject,
                mockLogger,
                mockEncryptionManager
        );
        
        when(mockEncryptionManager.decrypt(eq(encryptedValue), eq(dataTag))).thenReturn(value);
        when(mockEncryptionManager.encrypt(eq(value), eq(dataTag))).thenReturn(encryptedValue);
        
    }
    
    @Test
    public void testSaveValue() {
        Map<String, Object> cachedValues = target.getCachedValues();
        assertFalse("Cached values should not contain the key", cachedValues.containsKey(key));
        
        when(mockSharedPreferences.contains(eq(key))).thenReturn(false);
        when(mockEditor.putString(eq(key), eq(encryptedValue))).thenReturn(mockEditor);
        
        target.saveValue(key, value);
        
        InOrder inOrder = inOrder(mockEditor);
        
        inOrder.verify(mockEditor).putString(eq(key), eq(encryptedValue));
        inOrder.verify(mockEditor).apply();
        
        Map<String, Object> cachedValuesAfter = target.getCachedValues();
        assertTrue("Cached values did not contain the key", cachedValuesAfter.containsKey(key));
        assertEquals("Cached values did not contain the value", value, cachedValuesAfter.get(key));
        
        assertEquals(key, behaviorSubject.blockingFirst());
    }
    
    @Test
    public void testGetValue() {
        when(mockSharedPreferences.contains(eq(key))).thenReturn(true);
        when(mockSharedPreferences.getString(eq(key), isNull())).thenReturn(encryptedValue);
        
        String value = target.getValue(key, String.class, null);
        
        assertEquals("Values don't match", this.value, value);
    }
    
}