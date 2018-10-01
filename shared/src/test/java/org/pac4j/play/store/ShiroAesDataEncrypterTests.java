package org.pac4j.play.store;

import org.junit.Test;
import org.pac4j.core.util.TestsConstants;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Tests the {@link ShiroAesDataEncrypter}
 *
 * @author Jerome Leleu
 * @since 6.1.0
 */
public class ShiroAesDataEncrypterTests implements TestsConstants {

    private final ShiroAesDataEncrypter encrypter =  new ShiroAesDataEncrypter();

    @Test
    public void testOK() {
        final byte[] encrypted = encrypter.encrypt(VALUE.getBytes(StandardCharsets.UTF_8));
        final byte[] decrypted = encrypter.decrypt(encrypted);
        assertEquals(VALUE, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    public void testSupportsNull() {
        assertNull(encrypter.encrypt(null));
        assertNull(encrypter.decrypt(null));
    }
}
