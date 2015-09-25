/*
 *      Copyright (C) 2015 Noorq, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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