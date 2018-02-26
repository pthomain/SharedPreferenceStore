package uk.co.glass_software.android.shared_preferences.encryption.manager.key;

import android.content.Context;
import android.support.annotation.Nullable;

import com.facebook.crypto.CryptoConfig;

import java.security.KeyStore;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule;
import uk.co.glass_software.android.shared_preferences.persistence.preferences.SharedPreferenceStore;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.CONFIG_STORE_NAME;
import static uk.co.glass_software.android.shared_preferences.persistence.PersistenceModule.IS_ENCRYPTION_KEY_SECURE;

@Module(includes = PersistenceModule.class)
public class KeyModule {
    
    public static final String KEY_ALIAS = "KEY_ALIAS";
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    
    private final String keyAlias;
    
    public KeyModule(Context context) {
        keyAlias = context.getApplicationContext().getPackageName() + "$$StoreKey";
    }
    
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
    @Named(KEY_ALIAS)
    String provideKeyAlias() {
        return keyAlias;
    }
    
    @Provides
    @Singleton
    RsaEncrypter provideRsaEncrypter(@Nullable KeyStore keyStore) {
        return new RsaEncrypter(keyStore, keyAlias);
    }
    
    @Provides
    @Singleton
    KeyPair provideKeyPair(@Named(CONFIG_STORE_NAME) SharedPreferenceStore sharedPreferenceStore) {
        return new KeyPair(sharedPreferenceStore);
    }
    
    @Provides
    @Singleton
    IsKeyPairEncrypted provideIsKeyPairEncrypted(@Named(CONFIG_STORE_NAME) SharedPreferenceStore store) {
        return new IsKeyPairEncrypted(store);
    }
    
    @Provides
    @Singleton
    CryptoConfig provideCryptoConfig() {
        return CryptoConfig.KEY_256;
    }
    
    @Provides
    @Singleton
    KeyPairProvider provideKeyPairProvider(RsaEncrypter rsaEncrypter,
                                           Logger logger,
                                           KeyPair keyPair,
                                           CryptoConfig cryptoConfig,
                                           IsKeyPairEncrypted isKeyPairEncrypted) {
        return new KeyPairProvider(
                rsaEncrypter,
                logger,
                keyPair,
                cryptoConfig,
                isKeyPairEncrypted
        );
    }
    
    @Provides
    @Singleton
    @Named(IS_ENCRYPTION_KEY_SECURE)
    Boolean provideIsEncryptionKeySecure(IsKeyPairEncrypted isKeyPairEncrypted) {
        return isKeyPairEncrypted.get(false);
    }
}
