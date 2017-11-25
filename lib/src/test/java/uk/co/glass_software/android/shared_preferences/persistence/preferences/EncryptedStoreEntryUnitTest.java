package uk.co.glass_software.android.shared_preferences.persistence.preferences;


import org.junit.Before;
import org.junit.Test;

import uk.co.glass_software.android.shared_preferences.Function;
import uk.co.glass_software.android.shared_preferences.StoreKey;
import uk.co.glass_software.android.shared_preferences.persistence.base.EncryptedStoreEntry;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EncryptedStoreEntryUnitTest {
    
    private EncryptedSharedPreferenceStore mockKeyValueStore;
    
    private TestEntry target;
    private StoreKey storeKey = StoreKey.TEST;
    
    @Before
    public void setUp() throws Exception {
        mockKeyValueStore = mock(EncryptedSharedPreferenceStore.class);
        
        target = new TestEntry(
                mockKeyValueStore,
                storeKey
        );
    }
    
    @Test
    public void testSave() {
        String someValue = "someValue";
        String someEncryptedValue = "someEncryptedValue";
        
        target.save(someValue);
        
        verify(mockKeyValueStore).saveValue(eq(storeKey.getUniqueKey()), eq(someEncryptedValue));
    }
    
    @Test
    public void testSaveNull() {
        target.save(null);
        
        verify(mockKeyValueStore).saveValue(eq(storeKey.getUniqueKey()), isNull());
    }
    
    @Test
    public void testGet() {
        testOverloadedGetValue(givenValue -> target.get(), null);
    }
    
    @Test
    public void testGetValue() {
        final String someGivenValue = "someGivenValue";
        testOverloadedGetValue(givenValue -> target.get(someGivenValue), someGivenValue);
    }
    
    private void testOverloadedGetValue(Function<String, String> lambda,
                                        String givenValue) {
        String someValue = "someValue";
        String someDecryptedValue = "someDecryptedValue";
        
        when(mockKeyValueStore.getValue(
                eq(storeKey.getUniqueKey()),
                eq(String.class),
                eq(givenValue)
             )
        ).thenReturn(someValue);
        
        String result = lambda.get(null);
        
        verify(mockKeyValueStore).getValue(
                eq(storeKey.getUniqueKey()),
                eq(String.class),
                eq(givenValue)
        );
        
        assertEquals("EncryptedStoreEntry.get() returned the wrong value",
                     someDecryptedValue,
                     result
        );
    }
    
    private class TestEntry extends EncryptedStoreEntry {
        private TestEntry(EncryptedSharedPreferenceStore store,
                          StoreKey storeKey) {
            super(store, storeKey);
        }
    }
}