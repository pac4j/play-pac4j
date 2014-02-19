/*
  Copyright 2012 - 2014 Jerome Leleu

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

import org.apache.commons.lang3.StringUtils;
import org.pac4j.cas.logout.NoLogoutHandler;
import org.pac4j.core.context.WebContext;
import org.pac4j.play.java.JavaWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles logout requests from CAS server.
 * 
 * @author Jerome Leleu
 * @since 1.1.0
 */
public final class PlayLogoutHandler extends NoLogoutHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayLogoutHandler.class);
    
    @Override
    public void destroySession(final WebContext context) {
        final String logoutRequest = context.getRequestParameter("logoutRequest");
        logger.debug("logoutRequest : {}", logoutRequest);
        final String ticket = StringUtils.substringBetween(logoutRequest, "SessionIndex>", "</");
        logger.debug("extract ticket : {}", ticket);
        final String sessionId = (String) StorageHelper.get(ticket);
        logger.debug("found sessionId : {}", sessionId);
        StorageHelper.removeProfile(sessionId);
        StorageHelper.remove(ticket);
    }
    
    @Override
    public void recordSession(final WebContext context, final String ticket) {
        logger.debug("ticket : {}", ticket);
        final JavaWebContext javaWebContext = (JavaWebContext) context;
        final String sessionId = javaWebContext.getSession().get(Constants.SESSION_ID);
        logger.debug("save sessionId : {}", sessionId);
        StorageHelper.save(ticket, sessionId, Config.getProfileTimeout());
    }
}
