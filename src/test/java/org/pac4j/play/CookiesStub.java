package org.pac4j.play;

import java.util.ArrayList;
import java.util.Iterator;

import play.mvc.Http.Cookie;
import play.mvc.Http.Cookies;

/**
 * 
 * @author furkan yavuz
 * @since 2.1.0
 */
@SuppressWarnings("serial")
public class CookiesStub extends ArrayList<Cookie> implements Cookies {

	public CookiesStub(Cookie cookie) {
		super();
		this.add(cookie);
	}

	@Override
	public Iterator<Cookie> iterator() {
		return null;
	}

	@Override
	public Cookie get(String name) {
		return new Cookie(name, name, null, name, name, false, false);
	}	
}
