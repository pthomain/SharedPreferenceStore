package uk.co.glass_software.android.shared_preferences.encryption.manager.conceal;

import com.facebook.crypto.CryptoConfig;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.security.SecureRandom;

import uk.co.glass_software.android.shared_preferences.encryption.manager.key.RsaEncryptedKeyPairProvider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecureKeyChainUnitTest {
    
    private final CryptoConfig cryptoConfig = CryptoConfig.KEY_256;
    private SecureRandom mockSecureRandom;
    
    private RsaEncryptedKeyPairProvider mockKeyProvider;
    
    private SecureKeyChain target;
    
    @Before
    public void setUp() throws Exception {
        mockKeyProvider = mock(RsaEncryptedKeyPairProvider.class);
        mockSecureRandom = mock(SecureRandom.class);
        
        target = new SecureKeyChain(
                cryptoConfig,
                mockKeyProvider,
                () -> mockSecureRandom
        );
    }
    
    @Test
    public void testGetCipherKey() throws Exception {
        byte[] bytes = {1, 2, 3, 4, 5};
        when(mockKeyProvider.getCipherKey()).thenReturn(bytes);
        
        assertEquals(bytes, target.getCipherKey());
    }
    
    @Test
    public void testGetMacKey() throws Exception {
        byte[] bytes = {1, 2, 3, 4, 5};
        when(mockKeyProvider.getMacKey()).thenReturn(bytes);
        
        assertEquals(bytes, target.getMacKey());
    }
    
    @Test
    public void testGetNewIV() throws Exception {
        byte[] bytes = target.getNewIV();
        
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(mockSecureRandom).nextBytes(captor.capture());
        
        assertEquals(captor.getValue(), bytes);
    }
    
    @Test
    public void testDestroyKeys() {
        target.destroyKeys();
        
        verify(mockKeyProvider).destroyKeys();
    }
    
    @Test
    public void testIsEncryptionKeySecureTrue() {
        when(mockKeyProvider.isEncryptionKeySecure()).thenReturn(true);
        
        assertTrue(target.isEncryptionKeySecure());
    }
    
    @Test
    public void testIsEncryptionKeySecureFalse() {
        when(mockKeyProvider.isEncryptionKeySecure()).thenReturn(false);
        
        assertFalse(target.isEncryptionKeySecure());
    }
}