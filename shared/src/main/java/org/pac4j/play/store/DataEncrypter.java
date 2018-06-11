package org.pac4j.play.store;

/**
 * A wrapper with encryption/decryption functions, used in session cookie generation in `PlayCookieStore`.
 *
 * @author Vidmantas Zemleris
 * @since 6.1.0
 */
public interface DataEncrypter {
    /**
     * Decrypt
     *
     * @param encryptedBytes
     * @return decrypted bytes
     */
    byte[] decrypt(byte[] encryptedBytes);


    /**
     * Encrypt
     *
     * @param rawBytes
     * @return encrypted bytes
     */
    byte[] encrypt(byte[] rawBytes);
}
