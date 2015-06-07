/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers

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
  
  def createAccount = Action.async {
    
    var future = accountService.createAccount("organization", "team", "timezone");
    
    future.map(id => Ok("Created account: " + id))
  }

}