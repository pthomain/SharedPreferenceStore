package uk.co.glass_software.android.shared_preferences.demo.model;

import android.support.annotation.NonNull;

import uk.co.glass_software.android.shared_preferences.persistence.base.KeyValueStore;
import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry;

public class Counter extends StoreEntry<Integer> {
    public Counter(@NonNull KeyValueStore store) {
        super(store, Keys.COUNTER);
    }
}
