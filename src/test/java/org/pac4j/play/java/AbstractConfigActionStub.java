package org.pac4j.play.java;

import play.libs.F.Promise;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * @author furkan yavuz
 * @since 2.1.0
 */
public class AbstractConfigActionStub extends AbstractConfigAction {

	public AbstractConfigActionStub() {
		super();
	}

	@Override
	public Promise<Result> call(Context ctx) throws Throwable {
		return null;
	}
}
