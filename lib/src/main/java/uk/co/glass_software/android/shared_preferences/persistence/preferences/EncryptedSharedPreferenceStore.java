package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.keystore.KeyStoreManager;

public final class EncryptedSharedPreferenceStore extends SharedPreferenceStore {
    
    @Nullable
    private final KeyStoreManager keyStoreManager;
    
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
                                          @Nullable KeyStoreManager keyStoreManager) {
        super(sharedPreferences,
              base64Serialiser,
              customSerialiser,
              changeSubject,
              logger
        );
        this.keyStoreManager = keyStoreManager;
    }
    
    @Override
    synchronized void saveValueInternal(@NonNull String key,
                                        @Nullable Object value) {
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
                return (O) Boolean.valueOf(decrypted);
            }
            else if (Float.class.isAssignableFrom(objectClass)
                     || float.class.isAssignableFrom(objectClass)) {
                return (O) Float.valueOf(decrypted);
            }
            else if (Long.class.isAssignableFrom(objectClass)
                     || long.class.isAssignableFrom(objectClass)) {
                return (O) Long.valueOf(decrypted);
            }
            else if (Integer.class.isAssignableFrom(objectClass)
                     || int.class.isAssignableFrom(objectClass)) {
                return (O) Integer.valueOf(decrypted);
            }
            else if (String.class.isAssignableFrom(objectClass)) {
                return (O) decrypted;
            }
        }
        else {
           return super.getValueInternal(key, objectClass, defaultValue);
        }

        return null;
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
        
        return super.deserialise(decrypt(serialised), objectClass);
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
