package org.pac4j.play.store;

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
