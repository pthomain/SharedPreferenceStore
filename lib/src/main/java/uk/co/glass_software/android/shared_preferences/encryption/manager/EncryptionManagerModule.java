package uk.co.glass_software.android.shared_preferences.encryption.manager;

import android.content.Context;
import android.support.annotation.Nullable;

import com.facebook.android.crypto.keychain.AndroidConceal;

import java.security.KeyStore;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.key.KeyModule;
import uk.co.glass_software.android.shared_preferences.encryption.key.PostMSecureKeyProvider;
import uk.co.glass_software.android.shared_preferences.encryption.key.PreMSecureKeyProvider;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static uk.co.glass_software.android.shared_preferences.encryption.key.KeyModule.KEY_ALIAS;

@Module(includes = KeyModule.class)
public class EncryptionManagerModule {
    
    @Provides
    @Singleton
    @Nullable
    PreMEncryptionManager providePreMEncryptionManager(Logger logger,
                                                       PreMSecureKeyProvider secureKeyProvider,
                                                       @Named(KEY_ALIAS) String keyAlias,
                                                       @Nullable KeyStore keyStore,
                                                       Context applicationContext) {
        if (SDK_INT >= JELLY_BEAN_MR2
            && SDK_INT < M
            && keyStore != null) {
            return new PreMEncryptionManager(
                    logger,
                    keyStore,
                    keyAlias,
                    secureKeyProvider,
                    applicationContext
            );
        }
        return null;
    }
    
    @Provides
    @Singleton
    @Nullable
    PostMEncryptionManager providePostMEncryptionManager(Logger logger,
                                                         PostMSecureKeyProvider secureKeyProvider,
                                                         @Named(KEY_ALIAS) String keyAlias,
                                                         @Nullable KeyStore keyStore) {
        if (SDK_INT >= M
            && keyStore != null) {
            return new PostMEncryptionManager(
                    logger,
                    secureKeyProvider,
                    keyStore,
                    keyAlias
            );
        }
        return null;
    }
    
    @Provides
    @Singleton
    ConcealEncryptionManager provideConcealEncryptionManager(Logger logger,
                                                             Context applicationContext) {
        return new ConcealEncryptionManager(
                applicationContext,
                logger,
                AndroidConceal.get()
        );
    }
    
    @Provides
    @Singleton
    @Nullable
    EncryptionManager provideDefaultEncryptionManager(@Nullable PreMEncryptionManager preMEncryptionManager,
                                                      @Nullable PostMEncryptionManager postMEncryptionManager,
                                                      ConcealEncryptionManager concealEncryptionManager) {
        if (concealEncryptionManager.isAvailable()) {
            return concealEncryptionManager;
        }
        else if (postMEncryptionManager != null) {
            return postMEncryptionManager;
        }
        else {
            return preMEncryptionManager;
        }
    }
    
}
