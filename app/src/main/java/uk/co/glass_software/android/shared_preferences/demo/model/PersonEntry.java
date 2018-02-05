package uk.co.glass_software.android.shared_preferences.demo.model;

import android.support.annotation.NonNull;

import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry;

public class PersonEntry extends StoreEntry<Person> {
  
    public PersonEntry(@NonNull KeyValueStore store) {
        super(store, Keys.PERSON);
    }
    
}
