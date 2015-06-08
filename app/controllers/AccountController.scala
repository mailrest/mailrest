/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.data._
import play.api.data.Forms._
import play.api.mvc.ActionBuilder
import play.api.mvc.ActionTransformer
import play.api.mvc.Controller
import play.api.mvc.WrappedRequest
import scaldi.Injectable
import scaldi.Injector
import services.AccountService

class AccountController(implicit inj: Injector) extends Controller with Injectable {

  val accountService = inject [AccountService]
  
  val newAccountForm = Form(
    mapping(
      "organization" -> nonEmptyText,
      "team" -> text,
      "timezone" -> text
     )(NewAccountForm.apply)(NewAccountForm.unapply)
  )
  
  
  def createAccount = (AccountAction andThen AuthIt).async { 
     implicit request => {
    
      val newAccount = newAccountForm.bindFromRequest.get
      
      var future = accountService.createAccount(newAccount.organization, newAccount.team, newAccount.timezone);
      
      future.map(id => Ok("Created account: " + id))
    }
  }

}

case class NewAccountForm(organization: String, team: String, timezone:String) {
}