package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import org.junit.Before;
import org.junit.Test;

import uk.co.glass_software.android.shared_preferences.StoreKey;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StoreEntryTest {
    
    private final String someValue = "someValue";
    private final StoreKey key = StoreKey.TEST;
    
    private KeyValueStore mockStore;
    
    private StoreEntry<String> target;
    
    @Before
    public void setUp() throws Exception {
        mockStore = mock(KeyValueStore.class);
        target = new StoreEntry<>(
                mockStore,
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
        when(mockStore.getValue(eq(key.getUniqueKey()), eq(String.class), isNull())).thenReturn(someValue);
        
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
    
}