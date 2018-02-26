package uk.co.glass_software.android.shared_preferences.encryption.manager.key;

import android.support.annotation.NonNull;

import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry;

class IsKeyPairEncrypted extends StoreEntry<Boolean> {
    
    IsKeyPairEncrypted(@NonNull KeyValueStore store) {
        super(store, "IsKeyPairEncrypted", Boolean.class, true);
    }
    
}
