/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.Future

import com.mailrest.maildal.model.AccountUser
import com.mailrest.maildal.model.UserPermission

import controllers.action.AccountAction
import controllers.action.AccountAdminAction
import controllers.action.AccountReadAction
import controllers.action.AccountWriteAction
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.mvc._
import scaldi.Injectable
import scaldi.Injector

class AccountUserController(implicit inj: Injector) extends Controller with Injectable {

  val accountAction = inject [AccountAction]
  
  def readAction(accId: String) = accountAction andThen new AccountReadAction(accId)
  def writeAction(accId: String) = accountAction andThen new AccountWriteAction(accId)
  def adminAction(accId: String) = accountAction andThen new AccountAdminAction(accId)
    
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
      "firstName" -> optional(text), 
      "lastName" -> optional(text),
      "permission" -> nonEmptyText
    )(NewAccountUserForm.apply)(NewAccountUserForm.unapply)
  )  
  
  def create(accId: String) = adminAction(accId).async { 
    
    implicit request => {
    
       val form = newAccountUserForm.bindFromRequest.get
            
       val newAccountUser = new NewAccountUser(form.userId, form.email, 
           form.firstName.getOrElse(""), form.lastName.getOrElse(""),
           UserPermission.valueOf(form.permission))      
      
       Future.successful(Ok("Added user"))
    }
  }

  def find(accId: String, usrId: String) = readAction(accId).async {
    
    implicit request => {
    
      
       Future.successful(Ok("Added user"))
    }
    
  }
  
  def update(accId: String, usrId: String) = readAction(accId).async {
    
    implicit request => {
    
       Future.successful(Ok("Added user"))
    }
    
  }

  def delete(accId: String, usrId: String) = readAction(accId).async {
    
    implicit request => {
    
       Future.successful(Ok("Added user"))
    }
    
  }

}

case class NewAccountUser(userId: String, email: String, 
    firstName: String, lastName: String, 
    permission: UserPermission, confirmed: Boolean = false) extends AccountUser


case class NewAccountUserForm(userId: String, email: String, 
    firstName: Option[String], lastName: Option[String],
    permission: String) 
