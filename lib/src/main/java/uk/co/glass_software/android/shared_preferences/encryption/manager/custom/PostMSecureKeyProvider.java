package uk.co.glass_software.android.shared_preferences.encryption.manager.custom;

import android.support.annotation.Nullable;

import java.security.Key;
import java.security.KeyStore;

public class PostMSecureKeyProvider implements SecureKeyProvider {
    
    @Nullable
    private final KeyStore keyStore;
    
    private final String keyAlias;
    
    PostMSecureKeyProvider(@Nullable KeyStore keyStore,
                           String keyAlias) {
        this.keyStore = keyStore;
        this.keyAlias = keyAlias;
    }
    
    @Override
    @Nullable
    public Key getKey() throws Exception {
        return keyStore == null ? null : keyStore.getKey(keyAlias, null);
    }
    
}
