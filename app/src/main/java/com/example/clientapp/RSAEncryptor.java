package com.example.clientapp;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.BadPaddingException;
import android.util.Base64;

public class RSAEncryptor {

    private PublicKey publicKey;

    public RSAEncryptor(String modulusHex, String exponentHex) throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger modulus = new BigInteger(modulusHex, 16);
        BigInteger exponent = new BigInteger(exponentHex, 16);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        publicKey = factory.generatePublic(spec);
    }

    public String encrypt(String plainText) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}