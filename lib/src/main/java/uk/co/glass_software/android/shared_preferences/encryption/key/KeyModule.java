package uk.co.glass_software.android.shared_preferences.encryption.key;

import android.content.Context;
import android.support.annotation.Nullable;

import java.security.KeyStore;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.STORE_NAME;

@Module
public class KeyModule {
    
    public static final String KEY_ALIAS = "KEY_ALIAS";
   
    private final String keyAlias;
    
    public KeyModule(Context context) {
        keyAlias = context.getApplicationContext().getPackageName() + "$$StoreKey";
    }
    
    @Provides
    @Singleton
    @Named(KEY_ALIAS)
    String provideKeyAlias(){
        return keyAlias;
    }
    
    @Provides
    @Singleton
    @Nullable
    SavedEncryptedAesKey provideSavedEncryptedAesKey(@Named(STORE_NAME) SharedPreferenceStore sharedPreferenceStore,
                                                     @Nullable RsaEncrypter rsaEncrypter,
                                                     Logger logger) {
        if (SDK_INT < JELLY_BEAN_MR2 || SDK_INT >= M) {
            return null;
        }
        else {
            return new SavedEncryptedAesKey(
                    sharedPreferenceStore,
                    logger,
                    rsaEncrypter
            );
        }
    }
    
    @Provides
    @Singleton
    @Nullable
    RsaEncrypter provideRsaEncrypter(@Nullable KeyStore keyStore) {
        return new RsaEncrypter(keyStore, keyAlias);
    }
}
