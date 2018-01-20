package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Map;

import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.keystore.KeyStoreManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncryptedSharedPreferenceStoreUnitTest {
    
    private SharedPreferences mockSharedPreferences;
    private Base64Serialiser mockBase64Serialiser;
    private BehaviorSubject behaviorSubject;
    private SharedPreferences.Editor mockEditor;
    private KeyStoreManager mockKeyStoreManager;
    private Logger mockLogger;
    
    private final String key = "key";
    private final String value = "value";
    private final String encryptedValue = "encryptedValue";
    
    private EncryptedSharedPreferenceStore target;
    
    @Before
    public void setUp() throws Exception {
        
        mockSharedPreferences = mock(SharedPreferences.class);
        mockBase64Serialiser = mock(Base64Serialiser.class);
        behaviorSubject = BehaviorSubject.create();
        mockLogger = mock(Logger.class);
        mockKeyStoreManager = mock(KeyStoreManager.class);
        
        mockEditor = mock(SharedPreferences.Editor.class);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        
        target = new EncryptedSharedPreferenceStore(
                mockSharedPreferences,
                mockBase64Serialiser,
                behaviorSubject,
                mockLogger,
                mockKeyStoreManager
        );
        
        when(mockKeyStoreManager.decrypt(eq(encryptedValue))).thenReturn(value);
        when(mockKeyStoreManager.encrypt(eq(value))).thenReturn(encryptedValue);
        
    }
    
    @Test
    public void cache() {
        assertFalse(target.cache());
    }
    
    @Test
    public void testReadStoredValue() {
        assertEquals(value, target.readStoredValue(encryptedValue));
    }
    
    @Test
    public void testSaveValue() {
        Map<String, Object> cachedValues = target.getCachedValues();
        assertFalse("Cached values should not contain the key", cachedValues.containsKey(key));
    
        when(mockSharedPreferences.contains(eq(key))).thenReturn(false);
        when(mockEditor.putString(eq(key), eq(encryptedValue))).thenReturn(mockEditor);
        
        target.saveValue(key, value);
        
        InOrder inOrder = inOrder(mockEditor);
        
        inOrder.verify(mockEditor).putString(eq(key), eq(encryptedValue));
        inOrder.verify(mockEditor).apply();
        
        Map<String, Object> cachedValuesAfter = target.getCachedValues();
        assertTrue("Cached values did not contain the key", cachedValuesAfter.containsKey(key));
        assertEquals("Cached values did not contain the value", value, cachedValuesAfter.get(key));
        
        assertEquals(key, behaviorSubject.blockingFirst());
    }
    
    @Test
    public void testGetValue() {
        when(mockSharedPreferences.contains(eq(key))).thenReturn(true);
        when(mockSharedPreferences.getString(eq(key), isNull())).thenReturn(encryptedValue);
        
        String value = target.getValue(key, String.class, null);
        
        assertEquals("Values don't match", this.value, value);
    }
    
}