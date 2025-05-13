package com.antonioteca.cc42.utility;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    private static final String ALGORITHM = "AES";

    static {
        System.loadLibrary("cc42");
    }

    public static native String getSecretKeyFromJNI();

    public static String encrypt(String data) {
        SecretKeySpec keySpec = new SecretKeySpec(getSecretKeyFromJNI().getBytes(), ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.encodeToString(encryptedData, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String data) {
        SecretKeySpec keySpec = new SecretKeySpec(getSecretKeyFromJNI().getBytes(), ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.decode(data, Base64.NO_WRAP);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            return null;
        }
    }
}