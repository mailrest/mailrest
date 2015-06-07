/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers

import play.api.data._
import play.api.data.Forms._

import scala.concurrent.ExecutionContext
import play.api.mvc.Action
import play.api.mvc.Controller
import services.AccountService
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scaldi.Injectable
import scaldi.Injector

class AccountController(implicit inj: Injector) extends Controller with Injectable {

  val accountService = inject [AccountService]
  
  val newAccountForm = Form(
    mapping(
      "organization" -> nonEmptyText,
      "team" -> text,
      "timezone" -> text
     )(NewAccountForm.apply)(NewAccountForm.unapply)
  )
  
  def createAccount = Action.async { 
    implicit request => {
    
      val newAccount = newAccountForm.bindFromRequest.get
      
      var future = accountService.createAccount(newAccount.organization, newAccount.team, newAccount.timezone);
      
      future.map(id => Ok("Created account: " + id))
    }
  }

}

case class NewAccountForm(organization: String, team: String, timezone:String) {
}