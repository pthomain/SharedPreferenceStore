package uk.co.glass_software.android.shared_preferences.encryption.manager.key;

import android.util.Base64;

import com.facebook.android.crypto.keychain.SecureRandomFix;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.MacConfig;

import java.security.SecureRandom;

import uk.co.glass_software.android.shared_preferences.Logger;

public class KeyPairProvider {
    
    private final static String DELIMITER = "~";
    
    private final RsaEncrypter rsaEncrypter;
    private final Logger logger;
    private final KeyPair keyPair;
    private final CryptoConfig cryptoConfig;
    private final IsKeyPairEncrypted isKeyPairEncrypted;
    private Pair pair;
    
    KeyPairProvider(RsaEncrypter rsaEncrypter,
                    Logger logger,
                    KeyPair keyPair,
                    CryptoConfig cryptoConfig,
                    IsKeyPairEncrypted isKeyPairEncrypted) {
        this.rsaEncrypter = rsaEncrypter;
        this.logger = logger;
        this.keyPair = keyPair;
        this.cryptoConfig = cryptoConfig;
        this.isKeyPairEncrypted = isKeyPairEncrypted;
        initialise();
    }
    
    void initialise() {
        try {
            getOrGenerate();
        }
        catch (Exception e) {
            logger.e(this, e, "Could not initialise KeyPairProvider");
        }
    }
    
    public synchronized byte[] getCipherKey() throws Exception {
        return getOrGenerate().cipherKey;
    }
    
    public synchronized byte[] getMacKey() throws Exception {
        return getOrGenerate().macKey;
    }
    
    public synchronized void destroyKeys() {
        pair = null;
        keyPair.drop();
    }
    
    private synchronized Pair getOrGenerate() throws Exception {
        if (pair == null) {
            String string = keyPair.get();
            
            if (string == null) {
                pair = generateNewKeyPair();
                boolean isEncrypted = pair.encryptedCipherKey != null && pair.encryptedMacKey != null;
                
                byte[] cipherKey = isEncrypted ? pair.encryptedCipherKey : pair.cipherKey;
                byte[] macKey = isEncrypted ? pair.encryptedMacKey : pair.macKey;
                
                keyPair.saveInternal(toBase64(cipherKey) + DELIMITER + toBase64(macKey));
                isKeyPairEncrypted.save(isEncrypted);
                
                return pair;
            }
            else {
                String[] strings = string.split(DELIMITER);
                byte[] storedCipherKey = fromBase64(strings[0]);
                byte[] storedMacKey = fromBase64(strings[1]);
                
                if (isKeyPairEncrypted.get(false)) {
                    pair = new Pair(
                            rsaEncrypter.decrypt(storedCipherKey),
                            rsaEncrypter.decrypt(storedMacKey),
                            storedCipherKey,
                            storedMacKey
                    );
                }
                else {
                    pair = new Pair(
                            storedCipherKey,
                            storedMacKey,
                            null,
                            null
                    );
                }
            }
        }
    
        return pair;
    }
    
    private synchronized Pair generateNewKeyPair() throws Exception {
        byte[] cipherKey = new byte[cryptoConfig.keyLength];
        byte[] macKey = new byte[MacConfig.DEFAULT.keyLength];
        
        SecureRandom secureRandom = SecureRandomFix.createLocalSecureRandom();
        secureRandom.nextBytes(cipherKey);
        secureRandom.nextBytes(macKey);
        
        byte[] encryptedCipherKey = rsaEncrypter.encrypt(cipherKey);
        byte[] encryptedMacKey = rsaEncrypter.encrypt(macKey);
        
        if (encryptedCipherKey == null || encryptedMacKey == null) {
            logger.e(this, "RSA encrypter could not encrypt the keys");
            encryptedCipherKey = null;
            encryptedMacKey = null;
        }
        
        return new Pair(
                cipherKey,
                macKey,
                encryptedCipherKey,
                encryptedMacKey
        );
    }
    
    private String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    
    private byte[] fromBase64(String string) {
        return Base64.decode(string, Base64.DEFAULT);
    }
    
    private class Pair {
        private final byte[] cipherKey;
        private final byte[] macKey;
        private final byte[] encryptedCipherKey;
        private final byte[] encryptedMacKey;
        
        private Pair(byte[] cipherKey,
                     byte[] macKey,
                     byte[] encryptedCipherKey,
                     byte[] encryptedMacKey) {
            this.cipherKey = cipherKey;
            this.macKey = macKey;
            this.encryptedCipherKey = encryptedCipherKey;
            this.encryptedMacKey = encryptedMacKey;
        }
    }
}
