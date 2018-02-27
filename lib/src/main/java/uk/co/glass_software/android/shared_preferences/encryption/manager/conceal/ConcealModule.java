package uk.co.glass_software.android.shared_preferences.encryption.manager.conceal;

import android.content.Context;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.keychain.KeyChain;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.custom.PreMSecureKeyProvider;
import uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyModule;
import uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyPairProvider;

@Module(includes = KeyModule.class)
public class ConcealModule {
    
    @Provides
    @Singleton
    KeyChain provideKeyChain(CryptoConfig cryptoConfig,
                             KeyPairProvider keyPairProvider) {
        return new SecureKeyChain(
                cryptoConfig,
                keyPairProvider
        );
    }
    
    @Provides
    @Singleton
    ConcealEncryptionManager provideConcealEncryptionManager(Logger logger,
                                                             KeyChain keyChain,
                                                             Context applicationContext) {
        return new ConcealEncryptionManager(
                applicationContext,
                logger,
                keyChain,
                AndroidConceal.get()
        );
    }
    
}
