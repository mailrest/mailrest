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

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.runtime.universe

import com.mailrest.maildal.model.Account
import com.mailrest.maildal.model.AccountUser
import com.mailrest.maildal.model.UserPermission

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
import scaldi.Injector
import services.AccountService
import utils.ScalaHelper

class AccountController(implicit inj: Injector) extends AbstractAccountController {

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
              "businessName" -> account.businessName,
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
          "team" -> optional(text),
          "firstName" -> optional(text), 
          "lastName" -> optional(text)
          )(NewUserForm.apply)(NewUserForm.unapply),
      "businessName" -> optional(text),
      "timezone" -> optional(text)
     )(NewAccountForm.apply)(NewAccountForm.unapply)
  )
  
  
  def create = Action.async { 
     implicit request => {
    
      val newAccount = newAccountForm.bindFromRequest.get
      
      val newUser = new NewUser(newAccount.user.userId, 
          newAccount.user.email, 
          newAccount.user.team.getOrElse(""),
          newAccount.user.firstName.getOrElse(""), 
          newAccount.user.lastName.getOrElse(""))
      
      val future = accountService.createAccount(newUser, 
          newAccount.businessName.getOrElse(""), 
          newAccount.timezone.getOrElse(""));
      
      future.map(id => Ok(id))
    }
  }

  def find(accId: String) = readAction(accId).async {
    implicit request => {
      
      accountService.findAccount(accId).map { x => {
        
        x match {
          
          case Some(a) => Ok(Json.toJson(a))
          case None => NotFound
          
          
        }
        
      } }
      
    }
  }
  
  def delete(accId: String) = adminAction(accId).async {
    
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

case class NewUser(userId: String, email: String, team: String, firstName: String, lastName: String, permission: UserPermission = UserPermission.ADMIN, confirmed: Boolean = false) extends AccountUser

case class NewUserForm(userId: String, email: String, team: Option[String], firstName: Option[String], lastName: Option[String]) 

case class NewAccountForm(user: NewUserForm, businessName: Option[String], timezone: Option[String])
