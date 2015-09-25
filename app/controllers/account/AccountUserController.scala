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
import scala.annotation.implicitNotFound
import scala.concurrent.Future
import com.mailrest.maildal.model.AccountUser
import com.mailrest.maildal.model.UserPermission
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
import scaldi.Injector
import services.AccountService
import services.UserId

class AccountUserController(implicit inj: Injector) extends AbstractAccountController {

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
  
  val newAccountUserForm = Form(
    mapping (
      "userId" -> nonEmptyText,
      "email" -> email,
      "team" -> optional(text),
      "firstName" -> optional(text), 
      "lastName" -> optional(text),
      "permission" -> nonEmptyText
    )(NewAccountUserForm.apply)(NewAccountUserForm.unapply)
  )  
  
  val updateAccountUserForm = Form(
    mapping (
      "email" -> email,
      "team" -> optional(text),
      "firstName" -> optional(text), 
      "lastName" -> optional(text),
      "permission" -> nonEmptyText
    )(UpdateAccountUserForm.apply)(UpdateAccountUserForm.unapply)
  )    
  
  def create(accId: String) = adminAction(accId).async { 
    
    implicit request => {
    
       val form = newAccountUserForm.bindFromRequest.get
            
       val accountUser = new AccountUserBean(form.userId, form.email, form.team.getOrElse(""),
           form.firstName.getOrElse(""), form.lastName.getOrElse(""),
           UserPermission.valueOf(form.permission))      
      
       accountService.putAccountUser(accId, accountUser).map(x => Ok)
    }
  }

  def find(accId: String, userId: String) = readAction(accId).async {
    
    val id = new UserId(accId, userId)
    accountService.findAccountUser(id).map(x => {
      
      x match {
        
        case Some(au) => Ok(Json.toJson(au))
        case None => NotFound
        
      }
      
    } )
    
  }
  
  def update(accId: String, userId: String) = readAction(accId).async {
    
    implicit request => {
    
       val form = updateAccountUserForm.bindFromRequest.get
      
       val accountUser = new AccountUserBean(userId, form.email, form.team.getOrElse(""),
           form.firstName.getOrElse(""), form.lastName.getOrElse(""),
           UserPermission.valueOf(form.permission)) 
       
       accountService.putAccountUser(accId, accountUser).map(x => Ok)
    }
    
  }

  def delete(accId: String, userId: String) = readAction(accId).async {
    
      val id = new UserId(accId, userId)
      accountService.removeUser(id).map(x => Ok)
      
  }

}

case class AccountUserBean(userId: String, email: String, team:String,
    firstName: String, lastName: String, 
    permission: UserPermission, confirmed: Boolean = false) extends AccountUser


case class NewAccountUserForm(userId: String, email: String, team: Option[String],
    firstName: Option[String], lastName: Option[String],
    permission: String) 

    
case class UpdateAccountUserForm(email: String, team: Option[String],
    firstName: Option[String], lastName: Option[String],
    permission: String)     
    
    
    
    