package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.co.glass_software.android.boilerplate.log.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class LenientEncryptedStoreUnitTest {
    
    private KeyValueStore mockPlainTextStore;
    private KeyValueStore mockEncryptedStore;
    private Logger mockLogger;
    
    private LenientEncryptedStore target;
    
    @Before
    public void setUp() throws Exception {
        mockPlainTextStore = mock(KeyValueStore.class);
        mockEncryptedStore = mock(KeyValueStore.class);
        mockLogger = mock(Logger.class);
    }
    
    private void prepareTarget(boolean isEncryptionSupported) {
        target = new LenientEncryptedStore(
                mockPlainTextStore,
                mockEncryptedStore,
                isEncryptionSupported,
                mockLogger
        );
    }
    
    private void reset() {
        Mockito.reset(mockPlainTextStore, mockEncryptedStore);
    }
    
    @Test
    public void testIsEncryptionSupportedTrue() {
        prepareTarget(true);
        
        testIsEncryptionSupported(mockEncryptedStore, mockPlainTextStore);
    }
    
    @Test
    public void testIsEncryptionSupportedFalse() {
        prepareTarget(false);
        
        testIsEncryptionSupported(mockPlainTextStore, mockEncryptedStore);
    }
    
    private void testIsEncryptionSupported(KeyValueStore storeToUse,
                                           KeyValueStore storeNotToUse) {
        String someKey = "someKey";
        String someValue = "someValue";
        
        target.hasValue(someKey);
        verify(storeToUse).hasValue(eq(someKey));
        verify(storeNotToUse, never()).hasValue(any());
        reset();
        
        target.saveValue(someKey, someValue);
        verify(storeToUse).saveValue(eq(someKey), eq(someValue));
        verify(storeNotToUse, never()).saveValue(any(), any());
        reset();
        
        target.getValue(someKey, String.class, someValue);
        verify(storeToUse).getValue(eq(someKey), eq(String.class), eq(someValue));
        verify(storeNotToUse, never()).getValue(any(), any(), any());
        reset();
        
        target.deleteValue(someKey);
        verify(storeToUse).deleteValue(eq(someKey));
        verify(storeNotToUse, never()).deleteValue(any());
    }
}