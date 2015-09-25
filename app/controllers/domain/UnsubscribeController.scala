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
package controllers.account

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe
import com.mailrest.maildal.model.CallbackAction
import com.mailrest.maildal.secur.CallbackWebToken
import com.mailrest.maildal.secur.TokenManager
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result
import scaldi.Injectable
import scaldi.Injector
import services.AccountService
import com.mailrest.maildal.secur.UnsubscribeWebToken
import com.mailrest.maildal.repository.UnsubscribedRecipientRepository
import services.DomainId

class UnsubscribeController(implicit inj: Injector) extends Controller with Injectable {

  val unsubscribedRecipientRepository = inject [UnsubscribedRecipientRepository]
  
  val unsubscribeTokenManager = inject [TokenManager[UnsubscribeWebToken]]
  
  def decodeJwtLink(jwtLink: String): Option[UnsubscribeWebToken] = {
      try {
        Some(unsubscribeTokenManager.fromJwtLink(jwtLink))
      }
      catch { case e: Exception => None }
  }
  
  
  def callback(jwtLink: String) = Action.async { 
    
     implicit request => {
       
       decodeJwtLink(jwtLink) match {
         
         case Some(c) => store(c)
         case None => Future.successful(NotFound("invalid token"))
         
       }

    }
  }
  
  
  def store(cwt: UnsubscribeWebToken)(implicit reauest: Request[AnyContent]): Future[Result] = {
    
    val id = new DomainId(cwt.getAccountId, cwt.getDomainId)
    
    unsubscribedRecipientRepository.unsubscribeRecipient(id, cwt.getEmailId).map { x => Ok }
    
  }
  
}