package org.pac4j.play.store;

/**
 * A DataEncrypter based on the Shiro library and AES encryption.
 *
 * @author Jerome Leleu
 * @since 6.1.0
 */
@Deprecated
public class ShiroAesDataEncrypter extends JdkAesDataEncrypter {
    public ShiroAesDataEncrypter(byte[] key) { super(key); }
    public ShiroAesDataEncrypter() { super(); }
}
