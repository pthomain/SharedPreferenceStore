package uk.co.glass_software.android.shared_preferences.encryption.key;

import android.support.annotation.Nullable;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

public class PreMSecureKeyProvider implements SecureKeyProvider {
    
    private static final String ENCRYPTION = "AES";
    
    @Nullable
    private final SavedEncryptedAesKey encryptedAesKey;
    
    PreMSecureKeyProvider(@Nullable SavedEncryptedAesKey encryptedAesKey) {
        this.encryptedAesKey = encryptedAesKey;
    }
    
    @Override
    @Nullable
    public Key getKey() throws Exception {
        if (encryptedAesKey == null) {
            throw new NullPointerException("Could not load encrypted AES key");
        }
        
        byte[] storedKey = encryptedAesKey.getBytes();
        
        if (storedKey == null) {
            return null;
        }
        
        return new SecretKeySpec(storedKey, ENCRYPTION);
    }
    
}
