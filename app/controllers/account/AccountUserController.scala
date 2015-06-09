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
import controllers.action.AccountAuthAction
import controllers.action.AccountAction

class AccountUserController(implicit inj: Injector) extends Controller with Injectable {

  val action = inject [AccountAction] andThen AccountAuthAction
    
  def create = action.async { 
    
     implicit request => {
    
       Future.successful(Ok("Added user"))
    }
  }

}