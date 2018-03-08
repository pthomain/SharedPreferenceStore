package uk.co.glass_software.android.shared_preferences.encryption.manager.conceal;

import com.facebook.android.crypto.keychain.SecureRandomFix;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;

import java.security.SecureRandom;

import uk.co.glass_software.android.shared_preferences.encryption.manager.key.RsaEncryptedKeyPairProvider;

public class SecureKeyChain implements KeyChain {
    
    private final CryptoConfig mCryptoConfig;
    private final SecureRandom mSecureRandom;
    private final RsaEncryptedKeyPairProvider keyPairProvider;
    
    SecureKeyChain(CryptoConfig config,
                   RsaEncryptedKeyPairProvider keyPairProvider) {
        this.keyPairProvider = keyPairProvider;
        mSecureRandom = SecureRandomFix.createLocalSecureRandom();
        mCryptoConfig = config;
    }
    
    @Override
    public synchronized byte[] getCipherKey() throws KeyChainException {
        try {
            return keyPairProvider.getCipherKey();
        }
        catch (Exception e) {
            throw new KeyChainException("Could not retrieve cipher key", e);
        }
    }
    
    @Override
    public byte[] getMacKey() throws KeyChainException {
        try {
            return keyPairProvider.getMacKey();
        }
        catch (Exception e) {
            throw new KeyChainException("Could not retrieve MAC key", e);
        }
    }
    
    @Override
    public byte[] getNewIV() throws KeyChainException {
        byte[] iv = new byte[mCryptoConfig.ivLength];
        mSecureRandom.nextBytes(iv);
        return iv;
    }
    
    @Override
    public synchronized void destroyKeys() {
        keyPairProvider.destroyKeys();
    }
    
}
