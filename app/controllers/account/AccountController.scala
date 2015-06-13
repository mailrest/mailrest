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
import com.mailrest.maildal.model.AccountUser
import com.mailrest.maildal.model.UserPermission

class AccountController(implicit inj: Injector) extends Controller with Injectable {

  val readAction = inject [AccountAction] andThen AccountReadAction
  val writeAction = inject [AccountAction] andThen AccountWriteAction
  val adminAction = inject [AccountAction] andThen AccountAdminAction
  
  val accountService = inject [AccountService]
  
  val newAccountForm = Form(
    mapping(
      "user" -> mapping (
          "userId" -> nonEmptyText,
          "email" -> email,
          "firstName" -> optional(text), 
          "lastName" -> optional(text)
          )(NewUserForm.apply)(NewUserForm.unapply),
      "organization" -> optional(text),
      "team" -> optional(text),
      "timezone" -> optional(text)
     )(NewAccountForm.apply)(NewAccountForm.unapply)
  )
  
  
  def create = Action.async { 
     implicit request => {
    
      val newAccount = newAccountForm.bindFromRequest.get
      
      val newUser = new NewUser(newAccount.user.userId, 
          newAccount.user.email, 
          newAccount.user.firstName.getOrElse(""), 
          newAccount.user.lastName.getOrElse(""))
      
      val future = accountService.createAccount(newUser, 
          newAccount.organization.getOrElse(""), 
          newAccount.team.getOrElse(""), 
          newAccount.timezone.getOrElse(""));
      
      future.map(id => Ok("Created account: " + id))
    }
  }

}

case class NewUser(userId: String, email: String, firstName: String, lastName: String) extends AccountUser

case class NewUserForm(userId: String, email: String, firstName: Option[String], lastName: Option[String]) 

case class NewAccountForm(user: NewUserForm, organization: Option[String], team: Option[String], timezone: Option[String])
