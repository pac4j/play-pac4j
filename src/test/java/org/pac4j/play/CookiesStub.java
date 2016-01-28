/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
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
