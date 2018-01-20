package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Logger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SharedPreferenceStoreUnitTest {
    
    private SharedPreferences mockSharedPreferences;
    private Base64Serialiser mockBase64Serialiser;
    private BehaviorSubject behaviorSubject;
    private SharedPreferences.Editor mockEditor;
    private Logger mockLogger;
    private final String key = "someKey";
    private final String value = "someValue";
    
    private SharedPreferenceStore target;
    
    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mockSharedPreferences = mock(SharedPreferences.class);
        mockBase64Serialiser = mock(Base64Serialiser.class);
        behaviorSubject = BehaviorSubject.create();
        mockLogger = mock(Logger.class);
        
        mockEditor = mock(SharedPreferences.Editor.class);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        
        target = new SharedPreferenceStore(mockSharedPreferences,
                                           mockBase64Serialiser,
                                           behaviorSubject,
                                           mockLogger
        );
    }
    
    @Test
    public void cache() {
        assertTrue(target.cache());
    }
    
    @SuppressWarnings("unchecked")
    private void addValue() {
        Map map = new HashMap<>();
        map.put(key, value);
        
        when(mockSharedPreferences.getAll()).thenReturn(map);
        
        //Reset the cache
        target = new SharedPreferenceStore(mockSharedPreferences,
                                           mockBase64Serialiser,
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
    public void testBase64Read() {
        when(mockBase64Serialiser.isBase64(eq(value))).thenReturn(true);
        addValue();
        
        target.getValue(key, String.class, null);
        
        verify(mockBase64Serialiser).isBase64(eq(value));
        verify(mockBase64Serialiser).deserialise(eq(value));
    }
    
    @Test
    public void testNotBase64Read() {
        when(mockBase64Serialiser.isBase64(eq(value))).thenReturn(false);
        addValue();
        
        target.getValue(key, String.class, null);
        
        verify(mockBase64Serialiser).isBase64(eq(value));
        verify(mockBase64Serialiser, never()).deserialise(eq(value));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteValue() {
        addValue();
        
        Map<String, Object> cachedValues = target.getCachedValues();
        assertTrue("Cached values did not contain the key", cachedValues.containsKey(key));
        assertEquals("Cached values did not contain the value", value, cachedValues.get(key));
        
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
        
        Map<String, Object> cachedValues = target.getCachedValues();
        assertTrue("Cached values did not contain the key", cachedValues.containsKey(key));
        assertEquals("Cached values did not contain the value", value, cachedValues.get(key));
        
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
        assertTrue("Cached values did not contain the key", cachedValues.containsKey(key));
        assertEquals("Cached values did not contain the value", value, cachedValues.get(key));
        
        try {
            cachedValues.put("something", "something");
        }
        catch (Exception e) {
            return;
        }
        
        fail("Cached value map should be immutable");
    }
    
}