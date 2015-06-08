/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.Future

import controllers.AccountAction
import controllers.AuthIt
import play.api._
import play.api.mvc._
import scaldi.Injectable
import scaldi.Injector

class DomainController(implicit inj: Injector) extends Controller with Injectable {

  def createDomain = (AccountAction andThen AuthIt).async { 
    
     implicit request => {
    
      //val newAccount = newAccountForm.bindFromRequest.get
      
      //var future = accountService.createAccount(newAccount.organization, newAccount.team, newAccount.timezone);
      
      //future.map(id => Ok("Created account: " + id))
       
       Future.successful(Ok("Added domain"))
    }
  }

}