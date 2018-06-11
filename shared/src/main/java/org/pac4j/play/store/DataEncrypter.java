package org.pac4j.play.store;

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
