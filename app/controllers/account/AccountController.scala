/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Controller
import scaldi.Injectable
import scaldi.Injector
import services.AccountService
import scala.reflect.runtime.universe
import play.api.mvc.Action
import controllers.action.AccountAuthAction
import controllers.action.AccountAction

class AccountController(implicit inj: Injector) extends Controller with Injectable {

  val action = inject [AccountAction] andThen AccountAuthAction
  
  val accountService = inject [AccountService]
  
  val newAccountForm = Form(
    mapping(
      "organization" -> nonEmptyText,
      "team" -> text,
      "timezone" -> text
     )(NewAccountForm.apply)(NewAccountForm.unapply)
  )
  
  
  def create = action.async { 
     implicit request => {
    
      val newAccount = newAccountForm.bindFromRequest.get
      
      var future = accountService.createAccount(newAccount.organization, newAccount.team, newAccount.timezone);
      
      future.map(id => Ok("Created account: " + id))
    }
  }

}

case class NewAccountForm(organization: String, team: String, timezone:String) {
}