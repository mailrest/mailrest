/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.action

import com.mailrest.maildal.util.Base64
import play.api.mvc.RequestHeader
import play.api.mvc.Results

object BasicAuthHelper {

  private val BASIC_PREFIX = "basic " 
  
  def requestAuth = Results.Unauthorized.withHeaders(("WWW-Authenticate", "Basic realm=\"MailREST API\""))
  
  private def decodeBasicAuth(auth: String): Option[(String, String)] = {
      if (auth.length() < BASIC_PREFIX.length()) {
          return None
      }
      val basicPrefix = auth.substring(0, BASIC_PREFIX.length())
      if (!BASIC_PREFIX.equalsIgnoreCase(basicPrefix)) {
          return None
      }
      
      val basicBody = auth.substring(BASIC_PREFIX.length());
      val decodedBody = new String(Base64.INSTANCE.decode(basicBody), "UTF-8")
      
      val idx = decodedBody.indexOf(':');
      if (idx != -1) {
          val username = decodedBody.substring(0, idx);
          val password = decodedBody.substring(idx + 1, decodedBody.length());
          return Some(username, password)
      }
      None
  }
  
  def getCredentials(requestHeader: RequestHeader): Option[(String, String)] = {
      requestHeader.headers.get("authorization").map { basicAuth => decodeBasicAuth(basicAuth) }.getOrElse(None)
  }
  
}