package org.pac4j.play.store;

import org.apache.shiro.crypto.AesCipherService;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A DataEncrypter based on the Shiro library and AES encryption.
 *
 * @author Jerome Leleu
 * @since 6.1.0
 */
public class ShiroAesDataEncrypter implements DataEncrypter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private AesCipherService aesCipherService = new AesCipherService();

    private byte[] key;

    public ShiroAesDataEncrypter() {
        final String sKey = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        logger.info("Generated key: {}", sKey);
        this.key = sKey.getBytes(StandardCharsets.UTF_8);
    }

    public ShiroAesDataEncrypter(final String key) {
        CommonHelper.assertNotNull("key", key);
        logger.info("Using key: {}", key);
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decrypt(byte[] encryptedBytes) {
        if (encryptedBytes == null) {
            return null;
        } else {
            return aesCipherService.decrypt(encryptedBytes, key).getBytes();
        }
    }

    @Override
    public byte[] encrypt(byte[] rawBytes) {
        if (rawBytes == null) {
            return null;
        } else {
            return aesCipherService.encrypt(rawBytes, key).getBytes();
        }
    }
}
