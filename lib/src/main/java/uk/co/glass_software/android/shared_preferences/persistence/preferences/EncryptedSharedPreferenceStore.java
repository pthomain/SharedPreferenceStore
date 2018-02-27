package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.EncryptionManager;

public final class EncryptedSharedPreferenceStore extends SharedPreferenceStore {
    
    @Nullable
    private final EncryptionManager encryptionManager;
    
    public EncryptedSharedPreferenceStore(@NonNull SharedPreferences sharedPreferences,
                                          @NonNull Serialiser base64Serialiser,
                                          @Nullable Serialiser customSerialiser,
                                          @NonNull BehaviorSubject<String> changeSubject,
                                          @NonNull Logger logger) {
        this(sharedPreferences,
             base64Serialiser,
             customSerialiser,
             changeSubject,
             logger,
             null
        );
    }
    
    public EncryptedSharedPreferenceStore(@NonNull SharedPreferences sharedPreferences,
                                          @NonNull Serialiser base64Serialiser,
                                          @Nullable Serialiser customSerialiser,
                                          @NonNull BehaviorSubject<String> changeSubject,
                                          @NonNull Logger logger,
                                          @Nullable EncryptionManager encryptionManager) {
        super(sharedPreferences,
              base64Serialiser,
              customSerialiser,
              changeSubject,
              logger
        );
        this.encryptionManager = encryptionManager;
    }
    
    boolean isEncryptionSupported() {
        return encryptionManager != null;
    }
    
    @Override
    synchronized void saveValueInternal(@NonNull String key,
                                        @Nullable Object value) {
        saveToCache(key, value);
        
        if (value != null
            && (Boolean.class.isAssignableFrom(value.getClass())
                || boolean.class.isAssignableFrom(value.getClass())
                || Float.class.isAssignableFrom(value.getClass())
                || float.class.isAssignableFrom(value.getClass())
                || Long.class.isAssignableFrom(value.getClass())
                || long.class.isAssignableFrom(value.getClass())
                || Integer.class.isAssignableFrom(value.getClass())
                || int.class.isAssignableFrom(value.getClass())
                || String.class.isAssignableFrom(value.getClass()))) {
            super.saveValueInternal(key, encrypt(String.valueOf(value)));
        }
        else {
            super.saveValueInternal(key, value);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    synchronized <O> O getValueInternal(@NonNull String key,
                                        @NonNull Class<O> objectClass,
                                        @Nullable O defaultValue) {
        O valueInternal = null;
        if (Boolean.class.isAssignableFrom(objectClass)
            || boolean.class.isAssignableFrom(objectClass)
            || Float.class.isAssignableFrom(objectClass)
            || float.class.isAssignableFrom(objectClass)
            || Long.class.isAssignableFrom(objectClass)
            || long.class.isAssignableFrom(objectClass)
            || Integer.class.isAssignableFrom(objectClass)
            || int.class.isAssignableFrom(objectClass)
            || String.class.isAssignableFrom(objectClass)) {
            String serialised = super.getValueInternal(key, String.class, null);
            
            if (serialised == null) {
                return null;
            }
            
            String decrypted = decrypt(serialised);
            
            if (decrypted == null) {
                return null;
            }
            
            if (Boolean.class.isAssignableFrom(objectClass)
                || boolean.class.isAssignableFrom(objectClass)) {
                valueInternal = (O) Boolean.valueOf(decrypted);
            }
            else if (Float.class.isAssignableFrom(objectClass)
                     || float.class.isAssignableFrom(objectClass)) {
                valueInternal = (O) Float.valueOf(decrypted);
            }
            else if (Long.class.isAssignableFrom(objectClass)
                     || long.class.isAssignableFrom(objectClass)) {
                valueInternal = (O) Long.valueOf(decrypted);
            }
            else if (Integer.class.isAssignableFrom(objectClass)
                     || int.class.isAssignableFrom(objectClass)) {
                valueInternal = (O) Integer.valueOf(decrypted);
            }
            else if (String.class.isAssignableFrom(objectClass)) {
                valueInternal = (O) decrypted;
            }
        }
        else {
            valueInternal = super.getValueInternal(key, objectClass, defaultValue);
        }
        
        saveToCache(key, valueInternal);
        return valueInternal;
    }
    
    @Nullable
    @Override
    String serialise(@NonNull Object value) {
        String serialised = super.serialise(value);
        
        if (serialised == null) {
            return null;
        }
        
        return encrypt(serialised);
    }
    
    @Override
    <O> O deserialise(String serialised,
                      Class<O> objectClass) throws Serialiser.SerialisationException {
        if (serialised == null) {
            return null;
        }
        
        return super.deserialise(decrypt(serialised), key, objectClass);
    }
    
    @Nullable
    private String encrypt(@Nullable String clearText,
                           String key) {
        checkEncryptionAvailable();
        return clearText == null ? null : encryptionManager.encrypt(clearText, key);
    }
    
    @Nullable
    private <O> O decrypt(@Nullable String encrypted,
                          String key,
                          Class<O> targetClass) {
        checkEncryptionAvailable();
        if (encrypted == null) {
            return null;
        }
        else {
            String decrypted = encryptionManager.decrypt(encrypted, key);
            if(decrypted == null){
                return null;
            }
            return deserialise(decrypted, targetClass);
        }
    }
    
    private void checkEncryptionAvailable() {
        if (!isEncryptionSupported()) {
            throw new IllegalStateException("Encryption is not supported on this device");
        }
    }
    
}
