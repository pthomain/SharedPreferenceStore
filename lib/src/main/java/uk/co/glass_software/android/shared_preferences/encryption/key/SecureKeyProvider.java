package uk.co.glass_software.android.shared_preferences.encryption.key;

import android.support.annotation.Nullable;

import java.security.Key;

public interface SecureKeyProvider {
    
    @Nullable
    Key getKey() throws Exception;
    
}
