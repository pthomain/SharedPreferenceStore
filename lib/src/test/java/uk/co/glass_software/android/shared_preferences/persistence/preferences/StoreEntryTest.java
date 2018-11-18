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

import io.reactivex.Observable;
import uk.co.glass_software.android.boilerplate.utils.rx.On;
import uk.co.glass_software.android.shared_preferences.StoreKey;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueEntry;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StoreEntryTest {

    private final String someValue = "someValue";
    private final StoreKey key = StoreKey.TEST;

    private KeyValueStore mockStore;

    private KeyValueEntry<String> target;

    @Before
    public void setUp() throws Exception {
        mockStore = mock(KeyValueStore.class);
        target = new StoreEntry(
                mockStore,
                key,
                key
        );
    }

    @Test
    public void testSave() {
        target.save(someValue);

        verify(mockStore).saveValue(eq(key.getUniqueKey()), eq(someValue));
    }

    @Test
    public void testGet() {
        when(mockStore.getValue(eq(key.getUniqueKey()), eq(String.class))).thenReturn(someValue);

        assertEquals(someValue, target.get());
    }

    @Test
    public void testGetWithDefaultValue() {
        String defaultValue = "defaultValue";

        when(mockStore.getValue(eq(key.getUniqueKey()), eq(String.class), eq(defaultValue))).thenReturn(someValue);

        assertEquals(someValue, target.get(defaultValue));
    }

    @Test
    public void testDrop() {
        target.drop();

        verify(mockStore).deleteValue(eq(key.getUniqueKey()));
    }

    @Test
    public void testGetKey() {
        assertEquals(key.getUniqueKey(), target.getKey());
    }

    @Test
    public void testExistsTrue() {
        when(mockStore.hasValue(eq(key.getUniqueKey()))).thenReturn(true);

        assertTrue(target.exists());
    }

    @Test
    public void testExistsFalse() {
        when(mockStore.hasValue(eq(key.getUniqueKey()))).thenReturn(false);

        assertFalse(target.exists());
    }

    @Test
    public void testMaybeEmpty() {
        when(mockStore.hasValue(eq(key.getUniqueKey()))).thenReturn(false);

        assertFalse(target.maybe().isPresent());
    }

    @Test
    public void testMaybe() {
        when(mockStore.hasValue(eq(key.getUniqueKey()))).thenReturn(true);
        when(mockStore.getValue(eq(key.getUniqueKey()), eq(String.class))).thenReturn(someValue);

        assertEquals(someValue, target.maybe().get());
    }

    @Test
    public void testObserveWithWrongKey() {
        when(mockStore.observeChanges()).thenReturn(Observable.just("wrongKey"));

        target.observe(false, On.Trampoline.INSTANCE).subscribe();

        verify(mockStore, never()).getValue(any(), any());
    }

    @Test
    public void testObserve() {
        when(mockStore.observeChanges()).thenReturn(Observable.just(key.getUniqueKey()));
        when(mockStore.hasValue(eq(key.getUniqueKey()))).thenReturn(true);
        when(mockStore.getValue(eq(key.getUniqueKey()), eq(String.class))).thenReturn(someValue);

        assertTrue(target.observe(false, On.Trampoline.INSTANCE).blockingFirst().isPresent());
        assertEquals(someValue, target.observe(false, On.Trampoline.INSTANCE).blockingFirst().get());
    }

}