package uk.co.glass_software.android.shared_preferences.encryption.key;

public class PreMSecureKeyProvider implements SecureKeyProvider {
    
    @Override
    public byte[] getKey() {
        return new byte[0];
    }
    
}
