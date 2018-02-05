package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import io.reactivex.subjects.BehaviorSubject;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.keystore.KeyStoreManager;

public class EncryptedSharedPreferenceStore extends SharedPreferenceStore {
    
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
    protected final boolean cache() {
        return false;
    }
    
    @Override
    final Object readStoredValue(Object value) {
        checkEncryptionAvailable();
        return keyStoreManager.decrypt(value.toString());
    }
    
    @Override
    @SuppressLint("RestrictedApi")
    public synchronized final void saveValue(@NonNull String key,
                                             @Nullable Object value) {
        saveValueInternal(key, value);
    }
    
    @SuppressWarnings("unchecked")
    @RestrictTo(RestrictTo.Scope.TESTS)
    void saveValueInternal(@NonNull String key,
                           @Nullable Object value) {
        String serialised = null;
        
        if (value != null) {
            serialised = String.class.isInstance(value) ? value.toString() : serialise(value);
        }
        
        if (serialised == null) {
            logger.e(this, "Could not save value: " + key + " -> " + value);
            return;
        }
        
        super.saveValue(key, encrypt(serialised));
        saveToCache(key, value);
        
        logger.d(this, "Saving entry " + key + " -> " + value);
        changeSubject.onNext(key);
    }
    
    @Nullable
    private String serialise(@NonNull Object value) {
        Class<?> targetClass = value.getClass();
        try {
            if (customSerialiser != null && customSerialiser.canHandleType(targetClass)) {
                return customSerialiser.serialise(value);
            }
            else if (base64Serialiser.canHandleType(targetClass)) {
                return base64Serialiser.serialise(value);
            }
        }
        catch (Serialiser.SerialisationException e) {
            logger.e(this, e, "Could not serialise " + value);
        }
        
        return null;
    }
    
    @Nullable
    private <O> O deserialise(String value,
                              Class<O> objectClass) {
        if (Boolean.class.isAssignableFrom(objectClass)
            || boolean.class.isAssignableFrom(objectClass)) {
            return (O) Boolean.valueOf(value);
        }
        else if (Float.class.isAssignableFrom(objectClass)
                 || float.class.isAssignableFrom(objectClass)) {
            return (O) Float.valueOf(value);
        }
        else if (Long.class.isAssignableFrom(objectClass)
                 || long.class.isAssignableFrom(objectClass)) {
            return (O) Long.valueOf(value);
        }
        else if (Integer.class.isAssignableFrom(objectClass)
                 || int.class.isAssignableFrom(objectClass)) {
            return (O) Integer.valueOf(value);
        }
        else if (String.class.isAssignableFrom(objectClass)) {
            return (O) value;
        }
        
        try {
            if (base64Serialiser.canHandleSerialisedFormat(value)) {
                return base64Serialiser.deserialise(value, objectClass);
            }
            else if (customSerialiser != null && customSerialiser.canHandleSerialisedFormat(value)) {
                return customSerialiser.deserialise(value, objectClass);
            }
        }
        catch (Serialiser.SerialisationException e) {
            logger.e(this, e, "Could not deserialise " + value);
        }
        return null;
    }
    
    @Nullable
    @Override
    @SuppressLint("RestrictedApi")
    public synchronized final <O> O getValue(@NonNull String key,
                                             @NonNull Class<O> objectClass,
                                             @Nullable O defaultValue) {
        return getValueInternal(key, objectClass, defaultValue);
    }
    
    @RestrictTo(RestrictTo.Scope.TESTS)
    @SuppressWarnings("unchecked")
    <O> O getValueInternal(@NonNull String key,
                           @NonNull Class<O> objectClass,
                           @Nullable O defaultValue) {
        String value = super.getValue(key, String.class, null);
        
        return value == null
               ? defaultValue
               : decrypt(value, objectClass);
    }
    
    @Nullable
    private String encrypt(@Nullable String clearText) {
        checkEncryptionAvailable();
        return clearText == null ? null : keyStoreManager.encrypt(clearText);
    }
    
    @Nullable
    private <O> O decrypt(@Nullable String encrypted,
                          Class<O> targetClass) {
        checkEncryptionAvailable();
        return encrypted == null ? null : deserialise(keyStoreManager.decrypt(encrypted), targetClass);
    }
    
    private void checkEncryptionAvailable() {
        if (keyStoreManager == null) {
            throw new IllegalStateException("Encryption is not supported on this device");
        }
    }
    
}
