package uk.co.glass_software.android.shared_preferences.encryption.manager.conceal;


import android.content.Context;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.keychain.KeyChain;
import com.facebook.soloader.SoLoader;

import uk.co.glass_software.android.shared_preferences.Logger;
import uk.co.glass_software.android.shared_preferences.encryption.manager.BaseEncryptionManager;

public class ConcealEncryptionManager extends BaseEncryptionManager {
    
    private final boolean isAvailable;
    private final Crypto crypto;
    private Logger logger;
    
    ConcealEncryptionManager(Context context,
                             Logger logger,
                             KeyChain keyChain,
                             AndroidConceal androidConceal) {
        super(logger);
        this.logger = logger;
        SoLoader.init(context, false);
        
        // Creates a new Crypto object with default implementations of a key chain
        crypto = androidConceal.createDefaultCrypto(keyChain);
        
        // Check for whether the crypto functionality is available
        // This might fail if Android does not load libraries correctly.
        isAvailable = crypto.isAvailable();
        logger.d(this, "Conceal is" + (isAvailable ? "" : " NOT") + " available");
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    @Override
    public byte[] encryptBytes(byte[] toEncrypt,
                               String dataTag) {
        if (toEncrypt == null) {
            return null;
        }
        
        try {
            return crypto.encrypt(toEncrypt, Entity.create(dataTag));
        }
        catch (Exception e) {
            logger.e(this, e, "Could not encrypt the given bytes");
            return null;
        }
    }
    
    @Override
    public byte[] decryptBytes(byte[] toDecrypt,
                               String dataTag) {
        if (toDecrypt == null) {
            return null;
        }
        
        try {
            return crypto.decrypt(toDecrypt, Entity.create(dataTag));
        }
        catch (Exception e) {
            logger.e(this, "Could not decrypt the given bytes");
            return null;
        }
    }
}
