package org.pac4j.play.store;

/**
 * A wrapper with encryption/decryption functions, used in session cookie generation in `PlayCookieSessionStore`.
 *
 * @author Vidmantas Zemleris
 * @since 6.1.0
 */
public interface DataEncrypter {

    /**
     * Decrypt the data.
     *
     * @param encryptedBytes the encrypted bytes
     * @return decrypted bytes
     */
    byte[] decrypt(byte[] encryptedBytes);

    /**
     * Encrypt the data.
     *
     * @param rawBytes the raw bytes
     * @return encrypted bytes
     */
    byte[] encrypt(byte[] rawBytes);
}
