/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
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
    
    unsubscribedRecipientRepository.unsubscribeRecipient(cwt.getDomainId, cwt.getAccountId, cwt.getEmailId).map { x => Ok }
    
  }
  
}