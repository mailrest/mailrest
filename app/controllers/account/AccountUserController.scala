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

class AccountUserController(implicit inj: Injector) extends Controller with Injectable {

  val readAction = inject [AccountAction] andThen AccountReadAction
  val writeAction = inject [AccountAction] andThen AccountWriteAction
  val adminAction = inject [AccountAction] andThen AccountAdminAction
    
  def create = Action.async { 
    
     implicit request => {
    
       Future.successful(Ok("Added user"))
    }
  }

}