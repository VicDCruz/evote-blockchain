package mx.unam.iimas.mcic.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static mx.unam.iimas.mcic.utils.AsymmetricCryptographicHelper.decrypt;
import static mx.unam.iimas.mcic.utils.AsymmetricCryptographicHelper.encrypt;

public class JsonMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T fromJSONString(String json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toJSONString(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encryptedBytes(Object obj) {
        String json = toJSONString(obj);
        try {
            byte[] jsonBytes = requireNonNull(encrypt(json)).getBytes(UTF_8);
            return jsonBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T decryptedObject(String json, Class<T> tClass) {
        try {
            T obj = fromJSONString(decrypt(json), tClass);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
