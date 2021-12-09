package mx.unam.iimas.mcic.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static mx.unam.iimas.mcic.utils.JsonMapper.toJSONString;

public class AsymmetricCryptographicHelper {
    private final static String PATH_KEY = "";
    private final static String PUBLIC_KEY = "public.key";
    private final static String PRIVATE_KEY = "private.key";
    private final static String ASYMMETRIC_ALGORITHM = "RSA";

    public static void generateKeyPair() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        System.out.println("Generating new key pair for " + ASYMMETRIC_ALGORITHM);
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
        generator.initialize(4096);
        KeyPair pair = generator.generateKeyPair();
        storeKeys(pair);
    }

    private static void saveKeyToFile(String fileName, BigInteger modulus, BigInteger exponent) throws IOException {
        ObjectOutputStream ObjOutputStream = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(fileName)));
        try {
            ObjOutputStream.writeObject(modulus);
            ObjOutputStream.writeObject(exponent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ObjOutputStream.close();
        }
    }

    public static Key readKeyFromFile(String keyFileName) throws IOException {
        Key key = null;
        InputStream inputStream = new FileInputStream(keyFileName);
        ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(inputStream));
        try {
            BigInteger modulus = (BigInteger) objectInputStream.readObject();
            BigInteger exponent = (BigInteger) objectInputStream.readObject();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (keyFileName.startsWith("public"))
                key = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
            else
                key = keyFactory.generatePrivate(new RSAPrivateKeySpec(modulus, exponent));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            objectInputStream.close();
        }
        return key;
    }

    private static void storeKeys(KeyPair pair) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM);
        RSAPublicKeySpec publicKeySpec = keyFactory.getKeySpec(pair.getPublic(), RSAPublicKeySpec.class);
        RSAPrivateKeySpec privateKeySpec = keyFactory.getKeySpec(pair.getPrivate(), RSAPrivateKeySpec.class);
        saveKeyToFile("public.key", publicKeySpec.getModulus(), publicKeySpec.getPublicExponent());
        System.out.println("Public Key saved correctly");
        saveKeyToFile("private.key", privateKeySpec.getModulus(), privateKeySpec.getPrivateExponent());
        System.out.println("Private Key saved correctly");
    }

    public static boolean privateKeyExists() {
        File file = new File(PRIVATE_KEY);
        return file.isFile();
    }

    public static boolean publicKeyExists() {
        File file = new File(PUBLIC_KEY);
        return file.isFile();
    }

    public static boolean keysExists() {
        return privateKeyExists() && publicKeyExists();
    }

    private static PublicKey readPublicKey() throws IOException {
        return (PublicKey) readKeyFromFile(PATH_KEY + PUBLIC_KEY);
    }

    private static PrivateKey readPrivateKey() throws IOException {
        return (PrivateKey) readKeyFromFile(PATH_KEY + PRIVATE_KEY);
    }

    public static String encrypt(String plainText) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (!publicKeyExists()) {
            System.out.println("Public Key doesn't exist");
            return null;
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, readPublicKey());
        byte[] encryptedBytes = cipher.doFinal(requireNonNull(plainText).getBytes(UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String cipherText) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
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
