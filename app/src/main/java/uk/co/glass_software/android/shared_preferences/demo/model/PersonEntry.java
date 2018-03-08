package uk.co.glass_software.android.shared_preferences.demo.model;

import android.support.annotation.NonNull;

import uk.co.glass_software.android.shared_preferences.persistence.base.EncryptedStoreEntry;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.EncryptedSharedPreferenceStore;

public class PersonEntry extends EncryptedStoreEntry<Person> {
  
    public PersonEntry(@NonNull EncryptedSharedPreferenceStore store) {
        super(store, Keys.PERSON);
    }
    
}
