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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.boilerplate.log.Logger;
import uk.co.glass_software.android.boilerplate.preferences.Prefs;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SharedPreferenceStoreUnitTest {

    private Prefs mockSharedPrefs;
    private SharedPreferences mockSharedPreferences;
    private Serialiser mockBase64Serialiser;
    private Serialiser mockCustomSerialiser;
    private BehaviorSubject behaviorSubject;
    private SharedPreferences.Editor mockEditor;
    private Logger mockLogger;
    private final String key = "someKey";
    private final String value = "someValue";

    private SharedPreferenceStore target;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mockSharedPrefs = mock(Prefs.class);
        mockSharedPreferences = mock(SharedPreferences.class);
        mockBase64Serialiser = mock(Serialiser.class);
        mockCustomSerialiser = mock(Serialiser.class);
        behaviorSubject = BehaviorSubject.create();
        mockLogger = mock(Logger.class);

        mockEditor = mock(SharedPreferences.Editor.class);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockSharedPrefs.getFile()).thenReturn(mockSharedPreferences);

        target = new SharedPreferenceStore(
                mockSharedPrefs,
                mockBase64Serialiser,
                mockCustomSerialiser,
                behaviorSubject,
                mockLogger
        );
    }

    @SuppressWarnings("unchecked")
    private void addValue() {
        Map map = new HashMap<>();
        map.put(key, value);

        when(mockSharedPreferences.getAll()).thenReturn(map);
        when(mockSharedPreferences.contains(eq(key))).thenReturn(true);
        when(mockSharedPreferences.getString(eq(key), isNull())).thenReturn(value);
        when(mockSharedPrefs.getFile()).thenReturn(mockSharedPreferences);

        //Reset the cache
        target = new SharedPreferenceStore(
                mockSharedPrefs,
                mockBase64Serialiser,
                mockCustomSerialiser,
                behaviorSubject,
                mockLogger
        );
    }

    @Test
    public void testGetChangeSubject() {
        assertEquals("Wrong behaviour subject returned",
                behaviorSubject,
                target.observeChanges()
        );
    }

    @Test
    public void testBase64Read() throws Serialiser.SerialisationException {
        when(mockBase64Serialiser.canHandleType(eq(Serializable.class))).thenReturn(true);
        addValue();

        target.getValue(key, Serializable.class);

        verify(mockBase64Serialiser).deserialise(eq(value), eq(Serializable.class));
    }

    @Test
    public void testNotBase64Read() throws Serialiser.SerialisationException {
        when(mockBase64Serialiser.canHandleType(eq(Serializable.class))).thenReturn(false);
        addValue();

        target.getValue(key, Serializable.class);

        verify(mockBase64Serialiser, never()).deserialise(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteValue() {
        addValue();

        when(mockSharedPreferences.contains(eq(key))).thenReturn(true);
        when(mockEditor.remove(eq(key))).thenReturn(mockEditor);

        target.deleteValue(key);

        InOrder inOrder = inOrder(mockEditor);

        inOrder.verify(mockEditor).remove(eq(key));
        inOrder.verify(mockEditor).apply();

        Map<String, Object> cachedValuesAfter = target.getCachedValues();
        assertFalse("Cached values should not contain the key", cachedValuesAfter.containsKey(key));
    }

    @Test
    public void testSaveValue() {
        Map<String, Object> cachedValues = target.getCachedValues();
        assertFalse("Cached values should not contain the key", cachedValues.containsKey(key));

        when(mockSharedPreferences.contains(eq(key))).thenReturn(false);
        when(mockEditor.putString(eq(key), eq(value))).thenReturn(mockEditor);

        target.saveValue(key, value);

        InOrder inOrder = inOrder(mockEditor);

        inOrder.verify(mockEditor).putString(eq(key), eq(value));
        inOrder.verify(mockEditor).apply();

        Map<String, Object> cachedValuesAfter = target.getCachedValues();
        assertTrue("Cached values did not contain the key", cachedValuesAfter.containsKey(key));
        assertEquals("Cached values did not contain the value", value, cachedValuesAfter.get(key));
    }

    @Test
    public void testGetValue() {
        addValue();

        String value = target.getValue(key, String.class, null);
        assertEquals("Values don't match", this.value, value);
    }

    @Test
    public void testHasValue() {
        addValue();
        assertTrue("Store should have the key", target.hasValue(key));
    }

    @Test
    public void testGetCachedValues() {
        addValue();

        Map<String, Object> cachedValues = target.getCachedValues();

        try {
            cachedValues.put("something", "something");
        } catch (Exception e) {
            return;
        }

        fail("Cached value map should be immutable");
    }

}