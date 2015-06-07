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

//class AccountController(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends Controller with Injectable {

class AccountController(implicit inj: Injector) extends Controller with Injectable {

  val accountService = inject [AccountService]
  
  def createAccount = Action.async {
    
    var future = Future[Int](Int.box(123))
    
    future.map(id => Ok("Created account: " + id))
  }

}