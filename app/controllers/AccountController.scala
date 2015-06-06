/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers

import play.api._
import play.api.mvc._
import scaldi.Injectable
import scaldi.Injector
import services.AccountService
import scala.concurrent.ExecutionContext

class AccountController(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends Controller with Injectable {

  val accountService = inject [AccountService]
  
  def createAccount(organization: String, team: String, timezone: String) = Action.async {
    
    val future = accountService.createAccount(organization, team, timezone);
    
    future.map(id => Ok("Created account: " + id))
  }

}