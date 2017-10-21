package uk.co.glass_software.android.shared_preferences;

import uk.co.glass_software.android.shared_preferences.persistence.base.StoreEntry;

public enum StoreKey implements StoreEntry.UniqueKeyProvider, StoreEntry.ValueClassProvider{
    
    TEST;
    
    @Override
    public String getUniqueKey() {
        return "test";
    }
    
    @Override
    public Class getValueClass() {
        return String.class;
    }
}
