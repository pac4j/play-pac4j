package org.pac4j.play.java;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.reflect.Whitebox.getInternalState;

import org.junit.Test;
import org.pac4j.core.config.Config;

/**
 * @author furkan yavuz
 * @since 2.1.0
 */
public class SecureActionConstructorTest {
	
	private SecurityAction securityAction;
	private Config configMock;

	@Test
	public final void testConstructorConfig() {
		// given
		configMock = mock(Config.class);
		
		// when
		securityAction = new SecurityAction(configMock);
		
		// then
		Config config = getInternalState(securityAction, "config");
		assertEquals("Config field must be set.", configMock, config);
	}
}
