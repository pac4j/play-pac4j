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

/**
 * This class gathers all the constants for the OAuth support.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public interface OAuthConstants {
    
    public final static String OAUTH_SESSION_ID = "oauthSessionId";
    
    public final static String OAUTH_REQUESTED_URL = "oauthRequestedUrl";
    
    public final static String REDIRECT_URL_LOGOUT_PARAMETER_NAME = "url";
    
    public final static String PROVIDER_TYPE = "providerType";
    
    public final static String TARGET_URL = "targetUrl";
    
    public final static String SECRET_SUFFIX_SESSION_PARAMETER = "$secret";
    
    public final static String TOKEN_SUFFIX_SESSION_PARAMETER = "$token";
}
