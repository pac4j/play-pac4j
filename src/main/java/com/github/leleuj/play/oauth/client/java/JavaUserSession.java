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
package com.github.leleuj.play.oauth.client.java;

import org.scribe.model.Token;
import org.scribe.up.session.UserSession;

import play.mvc.Http.Session;

import com.github.leleuj.play.oauth.client.OAuthConstants;

/**
 * This class is the Java Session wrapper for Play. It handles only String or Token objects as the Play session only stores String and thus
 * requires a mapping from Object to String.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class JavaUserSession implements UserSession {
    
    private final Session session;
    
    public JavaUserSession(final Session session) {
        this.session = session;
    }
    
    public Object getAttribute(final String key) {
        final Object object = this.session.get(key);
        if (object != null) return object;
        
        final String secret = this.session.get(key + OAuthConstants.SECRET_SUFFIX_SESSION_PARAMETER);
        final String token = this.session.get(key + OAuthConstants.TOKEN_SUFFIX_SESSION_PARAMETER);
        if (secret != null || token != null) return new Token(token, secret);
        
        return null;
    }
    
    public void setAttribute(final String key, final Object value) {
        if (value instanceof String) {
            this.session.put(key, (String) value);
        } else if (value instanceof Token) {
            final Token scribeToken = (Token) value;
            final String secret = scribeToken.getSecret();
            this.session.put(key + OAuthConstants.SECRET_SUFFIX_SESSION_PARAMETER, secret);
            final String token = scribeToken.getToken();
            this.session.put(key + OAuthConstants.TOKEN_SUFFIX_SESSION_PARAMETER, token);
        } else
            throw new IllegalArgumentException("String and Token only supported in Play session");
    }
}
