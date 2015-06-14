/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.Future
import play.api._
import play.api.mvc._
import scaldi.Injectable
import scaldi.Injector
import controllers.action.AccountAction
import controllers.action.AccountAdminAction
import controllers.action.AccountReadAction
import controllers.action.AccountWriteAction

class AccountDomainController(implicit inj: Injector) extends Controller with Injectable {

  val accountAction = inject [AccountAction]
  
  def readAction(accId: String) = accountAction andThen new AccountReadAction(accId)
  def writeAction(accId: String) = accountAction andThen new AccountWriteAction(accId)
  def adminAction(accId: String) = accountAction andThen new AccountAdminAction(accId)
    
  def create = Action.async { 
    
     implicit request => {
       
       Future.successful(Ok("Added domain"))
    }
  }

}