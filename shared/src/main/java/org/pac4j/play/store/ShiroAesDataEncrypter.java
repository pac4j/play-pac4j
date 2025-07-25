package org.pac4j.play.store;

/**
 * A DataEncrypter based on the Shiro library and AES encryption.
 *
 * @author Jerome Leleu
 * @since 6.1.0
 */
@Deprecated
public class ShiroAesDataEncrypter implements DataEncrypter {
    private final JdkAesDataEncrypter delegate;

    public ShiroAesDataEncrypter(byte[] key) {
        this.delegate = new JdkAesDataEncrypter(key);
    }

    public ShiroAesDataEncrypter() {
        this.delegate = new JdkAesDataEncrypter();
    }

    @Override
    public byte[] encrypt(byte[] rawBytes) {
        return delegate.encrypt(rawBytes);
    }

    @Override
    public byte[] decrypt(byte[] encryptedBytes) {
        return delegate.decrypt(encryptedBytes);
    }
}
