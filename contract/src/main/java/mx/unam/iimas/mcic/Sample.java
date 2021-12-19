package mx.unam.iimas.mcic;

import mx.unam.iimas.mcic.configuration.DatabaseConfiguration;
import mx.unam.iimas.mcic.key.KeyType;
import mx.unam.iimas.mcic.models.Receipt;
import mx.unam.iimas.mcic.utils.AsymmetricCryptographicHelper;
import org.hibernate.Session;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.Query;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
