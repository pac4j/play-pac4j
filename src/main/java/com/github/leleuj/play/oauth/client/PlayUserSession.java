/*
  Copyright 2012 Jerome Leleu

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
package com.github.leleuj.play.oauth.client;

import org.scribe.model.Token;
import org.scribe.up.session.UserSession;

import play.mvc.Http.Session;

/**
 * This implementation uses the Play session for the user session and handles only objects of type String or Token as the Play session only
 * stores String and thus requires a mapping from Object to String.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class PlayUserSession implements UserSession {
    
    private static final String SECRET = "$secret";
    
    private static final String TOKEN = "$token";
    
    private final Session session;
    
    public PlayUserSession(final Session session) {
        this.session = session;
    }
    
    public Object getAttribute(final String key) {
        Object object = this.session.get(key);
        if (object != null) return object;
        
        String secret = this.session.get(key + SECRET);
        String token = this.session.get(key + TOKEN);
        if (secret != null || token != null) return new Token(token, secret);
        
        return null;
    }
    
    public void setAttribute(final String key, final Object value) {
        if (value instanceof String) {
            this.session.put(key, (String) value);
        } else if (value instanceof Token) {
            Token scribeToken = (Token) value;
            String secret = scribeToken.getSecret();
            this.session.put(key + SECRET, secret);
            String token = scribeToken.getToken();
            this.session.put(key + TOKEN, token);
        } else
            throw new IllegalArgumentException("String and Token only supported in Play session");
    }
}
