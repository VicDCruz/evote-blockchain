package mx.unam.iimas.mcic;

import mx.unam.iimas.mcic.utils.AsymmetricCryptographicHelper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Sample {
    public static void main(String[] args) throws NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        System.out.println("Hello, world!");
        if (!AsymmetricCryptographicHelper.keysExists())
            AsymmetricCryptographicHelper.generateKeyPair();
        String cipherText = AsymmetricCryptographicHelper.encrypt("Hello, world");
        System.out.println("Cipher text: " + cipherText);
        String plainText = AsymmetricCryptographicHelper.decrypt(cipherText);
        System.out.println("Plain text: " + plainText);
    }

}
