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

package uk.co.glass_software.android.shared_preferences.persistence.base;


import org.junit.Before;
import org.junit.Test;

import uk.co.glass_software.android.shared_preferences.StoreKey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StoreEntryUnitTest {
    
    private StoreEntry<String> target;
    private KeyValueStore mockStore;
    private StoreKey storeKey = StoreKey.TEST;
    
    private class TestEntry extends StoreEntry<String> {
        private TestEntry(KeyValueStore store) {
            super(store, storeKey, storeKey);
        }
    }
    
    @Before
    public void setUp() throws Exception {
        mockStore = mock(KeyValueStore.class);
        target = new TestEntry(mockStore);
    }
    
    @Test
    public void testSaveNull() {
        target.save(null);
        verify(mockStore).saveValue(eq(storeKey.getUniqueKey()), eq(null));
    }
    
    @Test
    public void testSave() {
        String value = "some string";
        target.save(value);
        verify(mockStore).saveValue(eq(storeKey.getUniqueKey()), eq(value));
    }
    
    @Test
    public void testGet() {
        String test = "test";
        when(mockStore.getValue(eq(storeKey.getUniqueKey()), eq(String.class), eq(null))).thenReturn(test);
        
        String result = target.get();
        
        verify(mockStore).getValue(eq(storeKey.getUniqueKey()), eq(String.class), eq(null));
        assertEquals("Strings don't match", test, result);
    }
    
    @Test
    public void testGetWithDefault() {
        String string = "abc";
        String defaultValue = "default";
        when(mockStore.getValue(eq(storeKey.getUniqueKey()), eq(String.class), eq(defaultValue)))
                .thenReturn(string);
        
        String result = target.get(defaultValue);
        
        verify(mockStore).getValue(eq(storeKey.getUniqueKey()), eq(String.class), eq(defaultValue));
        assertEquals("Strings don't match", string, result);
    }
    
    @Test
    public void testDrop() {
        target.drop();
        verify(mockStore).deleteValue(eq(storeKey.getUniqueKey()));
    }
    
    @Test
    public void testGetKey() {
        String key = target.getKey();
        assertEquals("Key is wrong", storeKey.getUniqueKey(), key);
    }
    
    @Test
    public void testGetKeyString() {
        String key = target.getKey();
        assertEquals("Key is wrong", storeKey.getUniqueKey(), key);
    }
    
    @Test
    public void testExists() throws Exception {
        when(mockStore.hasValue(eq(storeKey.getUniqueKey()))).thenReturn(true);
        assertTrue("Entry should exists", target.exists());

        when(mockStore.hasValue(eq(storeKey.getUniqueKey()))).thenReturn(false);
        assertFalse("Entry should exists", target.exists());

        verify(mockStore, times(2)).hasValue(eq(storeKey.getUniqueKey()));
    }
}