package org.pac4j.play;

import org.pac4j.core.exception.RequiresHttpAction;

/**
 * @author furkan yavuz
 * @since 2.1.0
 */
@SuppressWarnings("serial")
public class RequiresHttpActionStub extends RequiresHttpAction {

	public RequiresHttpActionStub(String message, int code) {
		super(message, code);
	}

}
