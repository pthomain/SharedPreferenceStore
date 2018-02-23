package uk.co.glass_software.android.shared_preferences.encryption.manager;

import android.content.Context;
import android.support.annotation.Nullable;

import java.security.KeyStore;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.key.KeyModule;
import uk.co.glass_software.android.shared_preferences.encryption.key.SavedEncryptedAesKey;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static uk.co.glass_software.android.shared_preferences.encryption.key.KeyModule.KEY_ALIAS;

@Module(includes = KeyModule.class)
public class EncryptionManagerModule {
    
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    
    @Provides
    @Singleton
    @Nullable
    KeyStore provideKeyStore() {
        if (SDK_INT < JELLY_BEAN_MR2) {
            return null;
        }
        else {
            try {
                KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
                keyStore.load(null);
                return keyStore;
            }
            catch (Exception e) {
                return null;
            }
        }
    }
    
    @Provides
    @Singleton
    @Nullable
    EncryptionManager provideKeyStoreManager(Logger logger,
                                             @Named(KEY_ALIAS) String keyAlias,
                                             @Nullable SavedEncryptedAesKey encryptedAesKey,
                                             @Nullable KeyStore keyStore,
                                             Context applicationContext) {
        if (SDK_INT < JELLY_BEAN_MR2 || keyStore == null) {
            return null;
        }
        else if (SDK_INT < M) {
            return new PreMEncryptionManager(
                    logger,
                    keyStore,
                    encryptedAesKey,
                    keyAlias,
                    applicationContext
            );
        }
        else {
            return new PostMEncryptionManager(
                    logger,
                    keyStore,
                    keyAlias
            );
        }
    }
    
}
