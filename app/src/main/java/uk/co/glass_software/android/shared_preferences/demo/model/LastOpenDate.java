package uk.co.glass_software.android.shared_preferences.demo.model;

import android.support.annotation.NonNull;
import uk.co.glass_software.android.shared_preferences.persistence.base.EncryptedStoreEntry;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.EncryptedSharedPreferenceStore;

import java.util.Date;

public class LastOpenDate extends EncryptedStoreEntry<Date> {

    public LastOpenDate(@NonNull EncryptedSharedPreferenceStore store) {
        super(store, Keys.LAST_OPEN_DATE);
    }

}
