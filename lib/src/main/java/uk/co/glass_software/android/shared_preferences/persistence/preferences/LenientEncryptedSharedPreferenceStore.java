package uk.co.glass_software.android.shared_preferences.persistence.preferences;

import android.support.annotation.NonNull;

import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;

public class LenientEncryptedSharedPreferenceStore implements KeyValueStore {

    @NonNull
    private final KeyValueStore internalStore;

    public LenientEncryptedSharedPreferenceStore(@NonNull SharedPreferenceStore plainTextStore,
                                                 @NonNull EncryptedSharedPreferenceStore encryptedStore,
                                                 Logger logger) {
        internalStore = encryptedStore.isEncryptionSupported() ? encryptedStore : plainTextStore;
        logger.d(
                this,
                "Encryption is"
                        + (encryptedStore.isEncryptionSupported() ? "" : " NOT")
                        + " supported"
        );
    }

    @Override
    public <V> V getValue(@NonNull String key,
                          @NonNull Class<V> valueClass,
                          V defaultValue) {
        return internalStore.getValue(key, valueClass, defaultValue);
    }

    @Override
    public <V> void saveValue(@NonNull String key, V value) {
        internalStore.saveValue(key, value);
    }

    @Override
    public boolean hasValue(@NonNull String key) {
        return internalStore.hasValue(key);
    }

    @Override
    public void deleteValue(@NonNull String key) {
        internalStore.deleteValue(key);
    }
}
