package uk.co.glass_software.android.shared_preferences.encryption.key;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.facebook.android.crypto.keychain.SecureRandomFix;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.MacConfig;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;

import java.security.SecureRandom;
import java.util.Arrays;

public class SecureKeyChain implements KeyChain {
    
    private static final String SHARED_PREF_NAME = "crypto";
    private static final String CIPHER_KEY_PREF = "cipher_key";
    private static final String MAC_KEY_PREF = "mac_key";
    
    private final CryptoConfig mCryptoConfig;
    
    private final SharedPreferences mSharedPreferences;
    private final SecureRandom mSecureRandom;
    
    private byte[] mCipherKey;
    private boolean mSetCipherKey;
    
    private byte[] mMacKey;
    private boolean mSetMacKey;
    
    SecureKeyChain(Context context,
                   CryptoConfig config) {
        String prefName = SHARED_PREF_NAME + "." + String.valueOf(config);
        mSharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        mSecureRandom = SecureRandomFix.createLocalSecureRandom();
        mCryptoConfig = config;
    }
    
    @Override
    public synchronized byte[] getCipherKey() throws KeyChainException {
        if (!mSetCipherKey) {
            mCipherKey = maybeGenerateKey(CIPHER_KEY_PREF, mCryptoConfig.keyLength);
        }
        mSetCipherKey = true;
        return mCipherKey;
    }
    
    @Override
    public byte[] getMacKey() throws KeyChainException {
        if (!mSetMacKey) {
            mMacKey = maybeGenerateKey(MAC_KEY_PREF, MacConfig.DEFAULT.keyLength);
        }
        mSetMacKey = true;
        return mMacKey;
    }
    
    @Override
    public byte[] getNewIV() throws KeyChainException {
        byte[] iv = new byte[mCryptoConfig.ivLength];
        mSecureRandom.nextBytes(iv);
        return iv;
    }
    
    @Override
    public synchronized void destroyKeys() {
        mSetCipherKey = false;
        mSetMacKey = false;
        if (mCipherKey != null) {
            Arrays.fill(mCipherKey, (byte) 0);
        }
        if (mMacKey != null) {
            Arrays.fill(mMacKey, (byte) 0);
        }
        mCipherKey = null;
        mMacKey = null;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(CIPHER_KEY_PREF);
        editor.remove(MAC_KEY_PREF);
        editor.commit();
    }
    
    /**
     * Generates a key associated with a preference.
     */
    private byte[] maybeGenerateKey(String pref, int length) throws KeyChainException {
        String base64Key = mSharedPreferences.getString(pref, null);
        if (base64Key == null) {
            // Generate key if it doesn't exist.
            return generateAndSaveKey(pref, length);
        }
        else {
            return decodeFromPrefs(base64Key);
        }
    }
    
    private byte[] generateAndSaveKey(String pref, int length) throws KeyChainException {
        byte[] key = new byte[length];
        mSecureRandom.nextBytes(key);
        // Store the session key.
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(
                pref,
                encodeForPrefs(key)
        );
        editor.commit();
        return key;
    }
    
    /**
     * Visible for testing.
     */
    byte[] decodeFromPrefs(String keyString) {
        if (keyString == null) {
            return null;
        }
        return Base64.decode(keyString, Base64.DEFAULT);
    }
    
    /**
     * Visible for testing.
     */
    String encodeForPrefs(byte[] key) {
        if (key == null) {
            return null;
        }
        return Base64.encodeToString(key, Base64.DEFAULT);
    }
}
