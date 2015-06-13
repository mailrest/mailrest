/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.runtime.universe
import com.mailrest.maildal.model.Account
import com.mailrest.maildal.model.AccountUser
import controllers.action.AccountAction
import controllers.action.AccountAdminAction
import controllers.action.AccountReadAction
import controllers.action.AccountWriteAction
import play.api.data.Form
import play.api.data.Forms.email
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Writes
import play.api.mvc.Action
import play.api.mvc.Controller
import scaldi.Injectable
import scaldi.Injector
import services.AccountService
import java.util.Collection
import utils.ScalaHelper
import com.mailrest.maildal.model.UserPermission

class AccountController(implicit inj: Injector) extends Controller with Injectable {

  val readAction = inject [AccountAction] andThen AccountReadAction
  val writeAction = inject [AccountAction] andThen AccountWriteAction
  val adminAction = inject [AccountAction] andThen AccountAdminAction
  
  val accountService = inject [AccountService]

  
  implicit val accountUserWrites = new Writes[AccountUser] {
      override def writes(au: AccountUser): JsValue = {
          Json.obj(
              "userId" -> au.userId,
              "email" -> au.email,
              "firstName" -> au.firstName,
              "lastName" -> au.lastName,
              "permission" -> au.permission.name,
              "confirmed" -> au.confirmed
          )
      }
  }
  
  implicit val accountWrites = new Writes[Account] {
      override def writes(account: Account): JsValue = {
          Json.obj(
              "accountId" -> account.accountId,
              "createdAt" -> account.createdAt,
              "organization" -> account.organization,
              "team" -> account.team,
              "timezone" -> account.timezone,
              "users" -> Json.arr(ScalaHelper.asSeq(account.users.values))
          )
      }
  }
  
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

  def find(accId: String) = readAction.async {
    implicit request => {
      
      accountService.findAccount(accId).map { x => {
        
        x match {
          
          case Some(a) => Ok(Json.toJson(a))
          case None => NotFound
          
          
        }
        
      } }
      
    }
  }
  
  def drop(accId: String) = adminAction.async {
    
    implicit request => {
      
      accountService.dropAccount(accId).map { dropped => {
        
        if (dropped) {
          Ok
        }
        else {
          NotFound
        }
        
        
      } }
      
    }
    
  }
  
}

case class NewUser(userId: String, email: String, firstName: String, lastName: String, permission: UserPermission = UserPermission.ADMIN) extends AccountUser

case class NewUserForm(userId: String, email: String, firstName: Option[String], lastName: Option[String]) 

case class NewAccountForm(user: NewUserForm, organization: Option[String], team: Option[String], timezone: Option[String])
