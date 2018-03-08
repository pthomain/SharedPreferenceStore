package uk.co.glass_software.android.shared_preferences.persistence.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import uk.co.glass_software.android.shared_preferences.persistence.preferences.EncryptedSharedPreferenceStore;

public abstract class EncryptedStoreEntry<C> extends StoreEntry<C> {
    
    @NonNull
    private final EncryptedSharedPreferenceStore store;
    private final boolean encryptKey = false; //FIXME, generates duplicates
    
    public EncryptedStoreEntry(@NonNull EncryptedSharedPreferenceStore store,
                               @NonNull String key,
                               @NonNull Class<C> valueClass) {
        super(store, key, valueClass);
        this.store = store;
    }
    
    public EncryptedStoreEntry(@NonNull EncryptedSharedPreferenceStore store,
                               @NonNull String key,
                               @NonNull Class<C> valueClass,
                               @Nullable C defaultValue) {
        super(store, key, valueClass, defaultValue);
        this.store = store;
    }
    
    public <K extends UniqueKeyProvider & ValueClassProvider> EncryptedStoreEntry(@NonNull EncryptedSharedPreferenceStore store,
                                                                                  @NonNull K keyProvider) {
        super(store, keyProvider);
        this.store = store;
    }
    
    public <K extends UniqueKeyProvider & ValueClassProvider> EncryptedStoreEntry(@NonNull EncryptedSharedPreferenceStore store,
                                                                                  @NonNull K keyProvider,
                                                                                  @Nullable C defaultValue) {
        super(store, keyProvider, defaultValue);
        this.store = store;
    }
    
    public EncryptedStoreEntry(@NonNull EncryptedSharedPreferenceStore store,
                               @NonNull UniqueKeyProvider keyProvider,
                               @NonNull ValueClassProvider valueClassProvider) {
        super(store, keyProvider, valueClassProvider);
        this.store = store;
    }
    
    public EncryptedStoreEntry(@NonNull EncryptedSharedPreferenceStore store,
                               @NonNull UniqueKeyProvider keyProvider,
                               @NonNull ValueClassProvider valueClassProvider,
                               @Nullable C defaultValue) {
        super(store, keyProvider, valueClassProvider, defaultValue);
        this.store = store;
    }
    
    @Override
    @NonNull
    String getKey() {
        if(encryptKey) {
            String encrypted = store.encrypt(keyString, keyString);
            return encrypted == null ? keyString : encrypted;
        }
        else{
            return super.getKey();
        }
    }
}
