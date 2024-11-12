package org.iol.lib;

import javax.crypto.Cipher;
import java.security.*;

import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAConnection {
    public PrivateKey privateKey;
    public String key;

    public RSAConnection() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            key = getPublicKeyString(keyPair.getPublic());
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String message) {
        return decrypt(message, privateKey);
    }

    public static String encryptMessage(String key, String message) {
        try {
            return encryptMessage(decodePublicKey(key), message);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey decodePublicKey(String publicKeyString) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static String encryptMessage(PublicKey publicKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String message, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedBytes = Base64.getDecoder().decode(message);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch(Throwable ignored) {
            return null;
        }
    }

    public static String getPublicKeyString(PublicKey key) {
        byte[] publicKeyBytes = key.getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }
}