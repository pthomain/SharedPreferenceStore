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

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import uk.co.glass_software.android.boilerplate.core.utils.log.Logger;
import uk.co.glass_software.android.shared_preferences.mumbo.encryption.EncryptionManager;
import uk.co.glass_software.android.shared_preferences.mumbo.store.EncryptedSharedPreferenceStore;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.serialisation.Serialiser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EncryptedSharedPreferenceStoreUnitTest {

    private Serialiser mockBase64Serialiser;
    private Serialiser mockCustomSerialiser;
    private EncryptionManager mockEncryptionManager;
    private KeyValueStore mockDelegateStore;

    private final String key = "key";
    private final String value = "value";
    private final String serialised = "serialised";
    private final String encryptedValue = "encryptedValue";

    private EncryptedSharedPreferenceStore target;

    @Before
    public void setUp() throws Exception {
        mockBase64Serialiser = mock(Serialiser.class);
        mockCustomSerialiser = mock(Serialiser.class);
        mockEncryptionManager = mock(EncryptionManager.class);
        mockDelegateStore = mock(KeyValueStore.class);

        when(mockCustomSerialiser.canHandleSerialisedFormat(any())).thenReturn(true);
        when(mockCustomSerialiser.canHandleType(any())).thenReturn(true);
        when(mockEncryptionManager.isEncryptionSupported()).thenReturn(true);

        target = new EncryptedSharedPreferenceStore(
                mock(Logger.class),
                mockBase64Serialiser,
                mockCustomSerialiser,
                mockDelegateStore,
                mockEncryptionManager,
                true
        );
    }

    @Test
    public void testSaveValue() throws Serialiser.SerialisationException {
        Map<String, Object> cachedValues = target.getCachedValues();
        assertFalse("Cached values should not contain the key", cachedValues.containsKey(key));

        when(mockCustomSerialiser.serialise(eq(value))).thenReturn(serialised);
        when(mockEncryptionManager.encrypt(eq(serialised), eq(key))).thenReturn(encryptedValue);

        target.saveValue(key, value);

        verify(mockDelegateStore).saveValue(eq(key), eq(encryptedValue));

        Map<String, Object> cachedValuesAfter = target.getCachedValues();
        assertTrue("Cached values did not contain the key", cachedValuesAfter.containsKey(key));
        assertEquals("Cached values did not contain the value", value, cachedValuesAfter.get(key));
    }

    @Test
    public void testGetValue() throws Serialiser.SerialisationException {
        when(mockDelegateStore.getValue(eq(key), eq(String.class))).thenReturn(encryptedValue);
        when(mockEncryptionManager.decrypt(eq(encryptedValue), eq(key))).thenReturn(serialised);
        when(mockCustomSerialiser.deserialise(eq(serialised), eq(String.class))).thenReturn(value);

        String value = target.getValue(key, String.class);

        assertEquals("Values don't match", this.value, value);
    }

}