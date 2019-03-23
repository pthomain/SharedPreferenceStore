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
import uk.co.glass_software.android.boilerplate.utils.log.Logger;
import uk.co.glass_software.android.boilerplate.utils.preferences.Prefs;
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager;
import uk.co.glass_software.android.shared_preferences.mumbo.store.EncryptedSharedPreferenceStore;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EncryptedSharedPreferenceStoreUnitTest {

    private Prefs mockSharedPrefs;
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

    @Before
    public void setUp() throws Exception {
        mockSharedPreferences = mock(SharedPreferences.class);
        mockSharedPrefs = mock(Prefs.class);
        mockBase64Serialiser = mock(Serialiser.class);
        mockCustomSerialiser = mock(Serialiser.class);
        behaviorSubject = BehaviorSubject.create();
        mockLogger = mock(Logger.class);
        mockEncryptionManager = mock(EncryptionManager.class);

        when(mockCustomSerialiser.canHandleSerialisedFormat(any())).thenReturn(true);
        when(mockCustomSerialiser.canHandleType(any())).thenReturn(true);

        mockEditor = mock(SharedPreferences.Editor.class);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockSharedPrefs.getFile()).thenReturn(mockSharedPreferences);

        target = new EncryptedSharedPreferenceStore(
                mockSharedPrefs,
                mockBase64Serialiser,
                mockCustomSerialiser,
                behaviorSubject,
                mockLogger,
                mockEncryptionManager
        );

        when(mockEncryptionManager.decrypt(eq(encryptedValue), eq(key))).thenReturn(value);
        when(mockEncryptionManager.encrypt(eq(value), eq(key))).thenReturn(encryptedValue);

    }

    @Test
    public void testSaveValue() throws Serialiser.SerialisationException {
        Map<String, Object> cachedValues = target.getCachedValues();
        assertFalse("Cached values should not contain the key", cachedValues.containsKey(key));

        when(mockSharedPreferences.contains(eq(key))).thenReturn(false);
        when(mockEditor.putString(eq(key), eq(encryptedValue))).thenReturn(mockEditor);
        when(mockCustomSerialiser.serialise(eq(value))).thenReturn(value);

        target.saveValue(key, value);

        verify(mockCustomSerialiser).serialise(eq(value));

        InOrder inOrder = inOrder(mockEditor);

        inOrder.verify(mockEditor).putString(eq(key), eq(encryptedValue));
        inOrder.verify(mockEditor).apply();

        Map<String, Object> cachedValuesAfter = target.getCachedValues();
        assertTrue("Cached values did not contain the key", cachedValuesAfter.containsKey(key));
        assertEquals("Cached values did not contain the value", value, cachedValuesAfter.get(key));

        assertEquals(key, behaviorSubject.blockingFirst());
    }

    @Test
    public void testGetValue() throws Serialiser.SerialisationException {
        when(mockSharedPreferences.contains(eq(key))).thenReturn(true);
        when(mockSharedPreferences.getString(eq(key), isNull())).thenReturn(encryptedValue);
        when(mockCustomSerialiser.deserialise(eq(value), eq(String.class))).thenReturn(value);

        String value = target.getValue(key, String.class);

        assertEquals("Values don't match", this.value, value);
    }

}