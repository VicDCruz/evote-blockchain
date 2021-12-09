package mx.unam.iimas.mcic.utils;

import mx.unam.iimas.mcic.configuration.DatabaseConfiguration;
import mx.unam.iimas.mcic.key.KeyType;
import mx.unam.iimas.mcic.models.UserKey;
import org.hibernate.Session;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.Query;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class AsymmetricCryptographicHelper {
    private final static String PATH_KEY = "";
    private final static String PUBLIC_KEY = "public.key";
    private final static String PRIVATE_KEY = "private.key";
    private final static String ASYMMETRIC_ALGORITHM = "RSA";

    public static void generateKeyPair() throws NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("Generating new key pair for " + ASYMMETRIC_ALGORITHM);
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
        generator.initialize(4096);
        KeyPair pair = generator.generateKeyPair();
        storeKeys(pair);
    }

    private static void saveKey(BigInteger modulus, BigInteger exponent, KeyType keyType) {
        Session session = DatabaseConfiguration.getSession();
        UserKey key = UserKey.builder()
                .modulus(modulus.toString())
                .exponent(exponent.toString())
                .keyType(keyType)
                .build();
        session.beginTransaction();
        session.save("UserKey", key);
        session.getTransaction().commit();
        session.close();
    }

    public static Key readKey(KeyType keyType) {
        Key key = null;
        Session session = DatabaseConfiguration.getSession();
        session.beginTransaction();
        Query query = session.createQuery("FROM UserKey WHERE keyType=:keyType").setParameter("keyType", keyType);
        UserKey userKey = (UserKey) query.getSingleResult();
        session.close();
        BigInteger modulus = new BigInteger(userKey.getModulus());
        BigInteger exponent = new BigInteger(userKey.getExponent());
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (keyType.equals(KeyType.PUBLIC_KEY))
                key = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
            else
                key = keyFactory.generatePrivate(new RSAPrivateKeySpec(modulus, exponent));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;
    }

    private static void storeKeys(KeyPair pair) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM);
        RSAPublicKeySpec publicKeySpec = keyFactory.getKeySpec(pair.getPublic(), RSAPublicKeySpec.class);
        RSAPrivateKeySpec privateKeySpec = keyFactory.getKeySpec(pair.getPrivate(), RSAPrivateKeySpec.class);
        saveKey(publicKeySpec.getModulus(), publicKeySpec.getPublicExponent(), KeyType.PUBLIC_KEY);
        System.out.println("Public Key saved correctly");
        saveKey(privateKeySpec.getModulus(), privateKeySpec.getPrivateExponent(), KeyType.PRIVATE_KEY);
        System.out.println("Private Key saved correctly");
    }

    public static boolean privateKeyExists() {
        Session session = DatabaseConfiguration.getSession();
        session.beginTransaction();
        Query query = session.createQuery("FROM UserKey WHERE keyType=:keyType").setParameter("keyType", KeyType.PRIVATE_KEY);
        List results = query.getResultList();
        session.close();
        return results.size() > 0;
    }

    public static boolean publicKeyExists() {
        Session session = DatabaseConfiguration.getSession();
        session.beginTransaction();
        Query query = session.createQuery("FROM UserKey WHERE keyType=:keyType").setParameter("keyType", KeyType.PUBLIC_KEY);
        List results = query.getResultList();
        session.close();
        return results.size() > 0;
    }

    public static boolean keysExists() {
        return privateKeyExists() && publicKeyExists();
    }

    private static PublicKey readPublicKey() {
        return (PublicKey) readKey(KeyType.PUBLIC_KEY);
    }

    private static PrivateKey readPrivateKey() {
        return (PrivateKey) readKey(KeyType.PRIVATE_KEY);
    }

    public static String encrypt(String plainText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (!publicKeyExists()) {
            System.out.println("Public Key doesn't exist");
            return null;
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, readPublicKey());
        byte[] encryptedBytes = cipher.doFinal(requireNonNull(plainText).getBytes(UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String cipherText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (!privateKeyExists()) {
            System.out.println("Private Key doesn't exist");
            return null;
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");
        cipher.init(Cipher.DECRYPT_MODE, readPrivateKey());
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText.getBytes()));
        return new String(decryptedBytes, UTF_8);
    }
}
