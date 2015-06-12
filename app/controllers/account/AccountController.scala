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
import controllers.action.AccountAction
import controllers.action.AccountAdminAction
import controllers.action.AccountReadAction
import controllers.action.AccountWriteAction

class AccountController(implicit inj: Injector) extends Controller with Injectable {

  val readAction = inject [AccountAction] andThen AccountReadAction
  val writeAction = inject [AccountAction] andThen AccountWriteAction
  val adminAction = inject [AccountAction] andThen AccountAdminAction
  
  val accountService = inject [AccountService]
  
  val newAccountForm = Form(
    mapping(
      "organization" -> nonEmptyText,
      "team" -> text,
      "timezone" -> text
     )(NewAccountForm.apply)(NewAccountForm.unapply)
  )
  
  
  def create = Action.async { 
     implicit request => {
    
      val newAccount = newAccountForm.bindFromRequest.get
      
      var future = accountService.createAccount(newAccount.organization, newAccount.team, newAccount.timezone);
      
      future.map(id => Ok("Created account: " + id))
    }
  }

}

case class NewAccountForm(organization: String, team: String, timezone:String) {
}