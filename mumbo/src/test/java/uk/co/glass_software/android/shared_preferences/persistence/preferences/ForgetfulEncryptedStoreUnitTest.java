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
import org.mockito.Mockito;

import uk.co.glass_software.android.boilerplate.core.utils.log.Logger;
import uk.co.glass_software.android.shared_preferences.mumbo.store.ForgetfulEncryptedStore;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ForgetfulEncryptedStoreUnitTest {
    
    private KeyValueStore mockEncryptedStore;
    private Logger mockLogger;
    
    private ForgetfulEncryptedStore target;
    
    @Before
    public void setUp() throws Exception {
        mockEncryptedStore = mock(KeyValueStore.class);
        mockLogger = mock(Logger.class);
    }
    
    private void prepareTarget(boolean isEncryptionSupported) {
        target = new ForgetfulEncryptedStore(
                mockEncryptedStore,
                isEncryptionSupported,
                mockLogger
        );
    }
    
    private void reset() {
        Mockito.reset(mockEncryptedStore);
    }
    
    @Test
    public void testIsEncryptionSupportedTrue() {
        prepareTarget(true);
        
        testIsEncryptionSupported(true);
    }
    
    @Test
    public void testIsEncryptionSupportedFalse() {
        prepareTarget(false);
        
        testIsEncryptionSupported(false);
    }
    
    private void testIsEncryptionSupported(boolean shouldUseInternalStore) {
        String someKey = "someKey";
        String someValue = "someValue";
        
        target.hasValue(someKey);
        if (shouldUseInternalStore) {
            verify(mockEncryptedStore).hasValue(eq(someKey));
        }
        else {
            verify(mockEncryptedStore, never()).hasValue(any());
        }
        reset();
        
        target.saveValue(someKey, someValue);
        if (shouldUseInternalStore) {
            verify(mockEncryptedStore).saveValue(eq(someKey), eq(someValue));
        }
        else {
            verify(mockEncryptedStore, never()).saveValue(any(), any());
        }
        reset();
        
        target.getValue(someKey, String.class, someValue);
        if (shouldUseInternalStore) {
            verify(mockEncryptedStore).getValue(eq(someKey), eq(String.class), eq(someValue));
        }
        else {
            verify(mockEncryptedStore, never()).getValue(any(), any(), any());
        }
        reset();
        
        target.deleteValue(someKey);
        if (shouldUseInternalStore) {
            verify(mockEncryptedStore).deleteValue(eq(someKey));
        }
        else {
            verify(mockEncryptedStore, never()).deleteValue(any());
        }
    }
}