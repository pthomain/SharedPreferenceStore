package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.keystore.KeyStoreManager;

public class EncryptedSharedPreferenceStore extends SharedPreferenceStore {
    
    @Nullable
    private final KeyStoreManager keyStoreManager;
    
    public EncryptedSharedPreferenceStore(@NonNull SharedPreferences sharedPreferences,
                                          @NonNull Base64Serialiser base64Serialiser,
                                          @NonNull BehaviorSubject<String> changeSubject,
                                          @NonNull Logger logger) {
        this(sharedPreferences, base64Serialiser, changeSubject, logger, null);
    }
    
    public EncryptedSharedPreferenceStore(@NonNull SharedPreferences sharedPreferences,
                                          @NonNull Base64Serialiser base64Serialiser,
                                          @NonNull BehaviorSubject<String> changeSubject,
                                          @NonNull Logger logger,
                                          @Nullable KeyStoreManager keyStoreManager) {
        super(sharedPreferences, base64Serialiser, changeSubject, logger);
        this.keyStoreManager = keyStoreManager;
        initCache();
    }
    
    @Override
    protected final boolean cache() {
        return false;
    }
    
    @Override
    final Object readStoredValue(Object value) {
        checkEncryptionAvailable();
        return keyStoreManager.decrypt(value.toString());
    }
    
    @Override
    public synchronized final void saveValue(@NonNull String key,
                                             @Nullable Object value) {
        if (value != null && !String.class.equals(value.getClass())) {
            throw new IllegalArgumentException("Only Strings are accepted");
        }
        super.saveValue(key, encrypt(value == null ? null : value.toString()));
        saveToCache(key, value);
        
        logger.d(this, "Saving entry " + key + " -> " + value);
        changeSubject.onNext(key);
    }
    
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public synchronized final <O> O getValue(@NonNull String key,
                                             @NonNull Class<O> objectClass,
                                             @Nullable O defaultValue) {
        if (!String.class.equals(objectClass)) {
            throw new IllegalArgumentException("Only Strings are accepted");
        }
        
        if (hasValue(key)) {
            return getFromCache(key);
        }
        
        O value = super.getValue(key, objectClass, defaultValue);
        return value == null ? null : (O) decrypt(value.toString());
    }
    
    @Nullable
    private String encrypt(@Nullable String clearText) {
        checkEncryptionAvailable();
        return clearText == null ? null : keyStoreManager.encrypt(clearText);
    }
    
    @Nullable
    private String decrypt(@Nullable String encrypted) {
        checkEncryptionAvailable();
        return encrypted == null ? null : keyStoreManager.decrypt(encrypted);
    }
    
    private void checkEncryptionAvailable() {
        if (keyStoreManager == null) {
            throw new IllegalStateException("Encryption is not supported on this device");
        }
    }
    
}
