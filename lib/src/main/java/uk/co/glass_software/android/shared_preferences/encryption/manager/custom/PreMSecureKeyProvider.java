package uk.co.glass_software.android.shared_preferences.encryption.manager.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.Nullable;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.Calendar;

import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyPairProvider;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static uk.co.glass_software.android.shared_preferences.encryption.manager.key.KeyModule.ANDROID_KEY_STORE;

public class PreMSecureKeyProvider implements SecureKeyProvider {
    
    private static final String ASYMMETRIC_ENCRYPTION = "RSA";
    private static final String ENCRYPTION = "AES";
    private final KeyPairProvider keyPairProvider;
    private final Context applicationContext;
    private final String keyAlias;
    private final Logger logger;
    
    @Nullable
    private final KeyStore keyStore;
    
    PreMSecureKeyProvider(KeyPairProvider keyPairProvider,
                          Context applicationContext,
                          Logger logger,
                          @Nullable KeyStore keyStore,
                          String keyAlias) {
        this.keyPairProvider = keyPairProvider;
        this.applicationContext = applicationContext;
        this.logger = logger;
        this.keyStore = keyStore;
        this.keyAlias = keyAlias;
        createNewKeyPairIfNeeded();
        keyPairProvider.initialise();
    }
    
    @Override
    public Key getKey() throws Exception {
        return new SecretKeySpec(keyPairProvider.getCipherKey(), ENCRYPTION);
    }
    
    @Override
    @TargetApi(JELLY_BEAN_MR2)
    public synchronized void createNewKeyPairIfNeeded() {
        try {
            if (keyStore != null && !keyStore.containsAlias(keyAlias)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 30);
                
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(applicationContext)
                        .setAlias(keyAlias)
                        .setSubject(new X500Principal("CN=" + keyAlias))
                        .setSerialNumber(BigInteger.TEN)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                        ASYMMETRIC_ENCRYPTION,
                        ANDROID_KEY_STORE
                );
                
                keyPairGenerator.initialize(spec);
                keyPairGenerator.generateKeyPair();
            }
        }
        catch (Exception e) {
            logger.e(this, e, "Could not create a new key");
        }
    }
    
}
