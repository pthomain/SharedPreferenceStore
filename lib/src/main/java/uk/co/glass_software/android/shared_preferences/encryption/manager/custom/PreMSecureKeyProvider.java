package uk.co.glass_software.android.shared_preferences.encryption.manager.custom;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyPairProvider;

public class PreMSecureKeyProvider implements SecureKeyProvider {
    
    private static final String ENCRYPTION = "AES";
    private final KeyPairProvider keyPairProvider;
    
    PreMSecureKeyProvider(KeyPairProvider keyPairProvider) {
        this.keyPairProvider = keyPairProvider;
    }
    
    @Override
    public Key getKey() throws Exception {
        return new SecretKeySpec(keyPairProvider.getCipherKey(), ENCRYPTION);
    }
    
}
