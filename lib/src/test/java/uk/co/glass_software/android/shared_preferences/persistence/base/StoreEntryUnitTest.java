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
        when(mockStore.getValue(eq(storeKey.getUniqueKey()), eq(String.class), eq(null))).thenReturn("something");
        assertTrue("Entry should exists", target.exists());
        when(mockStore.getValue(eq(storeKey.getUniqueKey()), eq(String.class), eq(null))).thenReturn(null);
        assertFalse("Entry should exists", target.exists());
        verify(mockStore, times(2)).getValue(eq(storeKey.getUniqueKey()), eq(String.class), eq(null));
    }
}