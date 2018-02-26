package uk.co.glass_software.android.shared_preferences.encryption.manager.custom;

import android.support.annotation.Nullable;

import java.security.Key;

public interface SecureKeyProvider {
    
    @Nullable
    Key getKey() throws Exception;
    
}
