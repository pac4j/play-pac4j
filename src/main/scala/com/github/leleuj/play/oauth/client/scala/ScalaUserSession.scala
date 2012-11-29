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
package com.github.leleuj.play.oauth.client.scala

import org.scribe.model.Token
import org.scribe.up.session.UserSession
import  play.api.mvc.Session
import com.github.leleuj.play.oauth.client.OAuthConstants

/**
 * This class is the Scala Session wrapper for Play. It handles only String or Token objects as the Play session only stores String and thus
 * requires a mapping from Object to String.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
 sealed class ScalaUserSession(s: Session) extends UserSession {
  var session: Session = s

  override def getAttribute(key: String): Object = {
    throw new IllegalAccessException("getAttribute not implemented")
  }
  
  override def setAttribute(key: String, value: Object): Unit = {
    if (value.isInstanceOf[String]) {
      session += (key -> value.toString)
    } else if (value.isInstanceOf[Token]) {
      val token = value.asInstanceOf[Token]
      session += (key + OAuthConstants.SECRET_SUFFIX_SESSION_PARAMETER -> token.getSecret.toString)
      session += (key + OAuthConstants.TOKEN_SUFFIX_SESSION_PARAMETER -> token.getToken.toString)
    } else {
      throw new IllegalArgumentException("String and Token only supported in Play session")    
    }
  }
  
  def getSession(): Session = {
    session
  }
}
