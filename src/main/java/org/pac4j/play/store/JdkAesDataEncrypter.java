package org.pac4j.play.store;

import org.pac4j.core.util.CommonHelper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * A DataEncrypter based on Java standard library AES encryption (no Shiro dependency).
 *
 * @author Rahul Malawadkar
 */
public final class JdkAesDataEncrypter implements DataEncrypter {

    private static final SecureRandom random = new SecureRandom();

    private final byte[] key;

    public JdkAesDataEncrypter(final byte[] key) {
        CommonHelper.assertNotNull("key", key);
        if (key.length != 16) {
            throw new IllegalArgumentException("AES key must be 16 bytes");
        }
        this.key = key.clone();
    }

    public JdkAesDataEncrypter() {
        // Generate random 16-byte AES key
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        this.key = bytes;
    }

    @Override
    public byte[] decrypt(byte[] encryptedBytes) {
        if (encryptedBytes == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption error", e);
        }
    }

    @Override
    public byte[] encrypt(byte[] rawBytes) {
        if (rawBytes == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(rawBytes);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption error", e);
        }
    }
}
