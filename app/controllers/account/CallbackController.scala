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

class CallbackController(implicit inj: Injector) extends Controller with Injectable {

  val accountService = inject [AccountService]
  
  val callbackTokenManager = inject [TokenManager[CallbackWebToken]]
  
  def decodeJwtLink(jwtLink: String): Option[CallbackWebToken] = {
      try {
        Some(callbackTokenManager.fromJwtLink(jwtLink))
      }
      catch { case e: Exception => None }
  }
  
  
  def callback = Action.async { 
    
     implicit request => {
       
       val form = callbackForm.bindFromRequest.get
       
       decodeJwtLink(form.jwtLink) match {
         
         case Some(c) => dispatch(c, form)
         case None => Future.successful(NotFound("invalid token"))
         
       }

    }
  }
  
  
  def dispatch(cwt: CallbackWebToken, form: CallbackForm)(implicit reauest: Request[AnyContent]): Future[Result] = {
    
    cwt.getAction match {
    
      case CallbackAction.CONFIRM_EMAIL => confirmEmail(cwt, form)
      case CallbackAction.UPDATE_PASSWORD => updatePassword(cwt, form)

    }
    
  }
  
  def confirmEmail(cwt: CallbackWebToken, form: CallbackForm)(implicit reauest: Request[AnyContent]): Future[Result] = {
    
    val future = accountService.confirmUser(cwt, form.newPassword)
    
    future.map(rs => Ok(cwt.getUserId))
    
  }

  def updatePassword(cwt: CallbackWebToken, form: CallbackForm)(implicit reauest: Request[AnyContent]): Future[Result] = {
    
    val future = accountService.updatePassword(cwt, form.newPassword)
    
    future.map(rs => Ok(cwt.getUserId))
    
  }

  val callbackForm = Form(
    mapping(
      "jwtLink" -> nonEmptyText, 
      "newPassword" -> nonEmptyText
     )(CallbackForm.apply)(CallbackForm.unapply)
  )
  
  case class CallbackForm(jwtLink: String, newPassword: String)
  
}