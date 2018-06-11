package org.pac4j.play.store;

/**
 * A dummy DataEncrypter with no op functions. Used, for example, to generate unencrypted session cookie in `PlayCookieStore`.
 *
 * @author Vidmantas Zemleris
 * @since 6.1.0
 */
public class NoOpDataEncrypter implements DataEncrypter {
    @Override
    public byte[] decrypt(byte[] encryptedBytes) {
        return encryptedBytes;
    }

    @Override
    public byte[] encrypt(byte[] rawBytes) {
        return rawBytes;
    }
}
