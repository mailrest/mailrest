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
package filters

import scala.concurrent.Future
import com.typesafe.scalalogging.slf4j.LazyLogging
import play.api.mvc.Filter
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results
import sun.misc.BASE64Decoder
import com.mailrest.maildal.util.Base64

object BasicAuthFilter extends Filter with LazyLogging {
  
    private lazy val unauthResult = Results.Unauthorized.withHeaders(("WWW-Authenticate", 
    "Basic realm=\"MailRest API\""))
    
    private lazy val passwordRequired = false
    private lazy val username = "someUsername"
    private lazy val password = "somePassword"
    
    private lazy val basicSt = "basic " 
    
    //This is needed if you are behind a load balancer or a proxy
    private def getUserIPAddress(request: RequestHeader): String = {
        return request.headers.get("x-forwarded-for").getOrElse(request.remoteAddress.toString)
    }
    
    private def logFailedAttempt(requestHeader: RequestHeader) = {
        logger.warn(s"IP address ${getUserIPAddress(requestHeader)} failed to log in, " +
            s"requested uri: ${requestHeader.uri}")
    }

    private def decodeBasicAuth(auth: String): Option[(String, String)] = {
        if (auth.length() < basicSt.length()) {
            return None
        }
        val basicReqSt = auth.substring(0, basicSt.length())
        if (basicReqSt.toLowerCase() != basicSt) {
            return None
        }
        val basicAuthSt = auth.replaceFirst(basicReqSt, "")
        val decodedAuthSt = new String(Base64.INSTANCE.decode(basicAuthSt), "UTF-8")
        val usernamePassword = decodedAuthSt.split(":")
        if (usernamePassword.length >= 2) {
            //account for ":" in passwords
            return Some(usernamePassword(0), usernamePassword.splitAt(1)._2.mkString)
        }
        None
    }

    def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): 
    Future[Result] = {
        if (!passwordRequired) {
            return nextFilter(requestHeader)
        }
        
        logger.info("BasicAuthFilter invoked")

        requestHeader.headers.get("authorization").map { basicAuth =>
            decodeBasicAuth(basicAuth) match {
                case Some((user, pass)) => {
                    if (username == user && password == pass) {
                        return nextFilter(requestHeader)
                    }
                }
                case _ => ;
            }
            logFailedAttempt(requestHeader)
            return Future.successful(unauthResult)
        }.getOrElse({
            logFailedAttempt(requestHeader)
            Future.successful(unauthResult)
        })

    }
}