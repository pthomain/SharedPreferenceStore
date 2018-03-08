package uk.co.glass_software.android.shared_preferences.encryption.manager;

import android.support.annotation.Nullable;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.conceal.ConcealEncryptionManager;
import uk.co.glass_software.android.shared_preferences.encryption.manager.conceal.ConcealModule;
import uk.co.glass_software.android.shared_preferences.encryption.manager.custom.CustomModule;
import uk.co.glass_software.android.shared_preferences.encryption.manager.custom.PostMEncryptionManager;
import uk.co.glass_software.android.shared_preferences.encryption.manager.custom.PreMEncryptionManager;

@Module(includes = {
        ConcealModule.class,
        CustomModule.class
})
public class EncryptionManagerModule {
    
    private final boolean fallbackToCustomEncryption;
    
    public EncryptionManagerModule(boolean fallbackToCustomEncryption) {
        this.fallbackToCustomEncryption = fallbackToCustomEncryption;
    }
    
    @Provides
    @Singleton
    @Nullable
    EncryptionManager provideDefaultEncryptionManager(@Nullable PreMEncryptionManager preMEncryptionManager,
                                                      @Nullable PostMEncryptionManager postMEncryptionManager,
                                                      ConcealEncryptionManager concealEncryptionManager,
                                                      Logger logger) {
        if (concealEncryptionManager.isAvailable() || !fallbackToCustomEncryption) {
            if (concealEncryptionManager.isAvailable()) {
                logger.d(this, "Using Conceal encryption manager");
            }
            else {
                logger.e(this, "Encryption is NOT supported: Conceal is not available and there is no fallback to custom encryption as per config");
            }
            return concealEncryptionManager;
        }
        else if (postMEncryptionManager != null) {
            logger.d(this, "Using post-M encryption manager");
            return postMEncryptionManager;
        }
        else {
            logger.d(this, "Using pre-M encryption manager");
            return preMEncryptionManager;
        }
    }
    
}
