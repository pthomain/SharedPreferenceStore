package uk.co.glass_software.android.shared_preferences.demo.model;

import android.support.annotation.NonNull;

import java.util.Date;

import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.EncryptedSharedPreferenceStore;

public class LastOpenDate extends StoreEntry<Date> {

    public LastOpenDate(@NonNull EncryptedSharedPreferenceStore store) {
        super(store, Keys.LAST_OPEN_DATE);
    }

}
