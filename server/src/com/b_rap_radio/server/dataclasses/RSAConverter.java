package com.b_rap_radio.server.dataclasses;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class RSAConverter {

    public static final int KEYSIZE = 4096;

    private static RSAConverter instance;

    public static RSAConverter getInstance() throws IllegalStateException {
        if(instance == null)
            instance = new RSAConverter();

        return instance;
    }


    KeyPair keyPair;

    private RSAConverter() throws IllegalStateException {
        if(!changeKeys())
            throw new IllegalStateException("Could not load keypair (NoSuchAlgorithmException on changeKeys)");
    }

    public boolean changeKeys() {
        final KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException ignored) {
            return false;
        }

        generator.initialize(KEYSIZE);
        keyPair = generator.generateKeyPair();
        return true;
    }

    public String decodeText(String cipheredText) throws BadPaddingException, IllegalBlockSizeException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) { // should never happen
            e.printStackTrace();
            return null;
        }

        byte[] clearArray = cipher.doFinal(Base64.getDecoder().decode(cipheredText));
        return new String(clearArray, StandardCharsets.UTF_8);
    }

    public RSAPublicKey getPublicKey(){
        return (RSAPublicKey) keyPair.getPublic();
    }
}
