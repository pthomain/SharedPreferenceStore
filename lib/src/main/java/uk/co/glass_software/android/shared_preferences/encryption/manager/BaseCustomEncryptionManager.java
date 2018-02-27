package uk.co.glass_software.android.shared_preferences.encryption.manager;

import android.support.annotation.Nullable;

import javax.crypto.Cipher;

import uk.co.glass_software.android.shared_preferences.Logger;

public abstract class BaseCustomEncryptionManager extends BaseEncryptionManager {
    
    protected BaseCustomEncryptionManager(Logger logger) {
        super(logger);
    }
    
    @Override
    @Nullable
    public byte[] encryptBytes(byte[] toEncrypt,
                               String dataTag) {
        if (toEncrypt == null) {
            return null;
        }
        
        try {
            Cipher cipher = getCipher(true);
            return cipher.doFinal(toEncrypt);
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
            Cipher cipher = getCipher(false);
            return cipher.doFinal(toDecrypt);
        }
        catch (Exception e) {
            logger.e(this, "Could not decrypt the given bytes");
            return null;
        }
    }
    
    protected abstract Cipher getCipher(boolean isEncrypt) throws Exception;
}
