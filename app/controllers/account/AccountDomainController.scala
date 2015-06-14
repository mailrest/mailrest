/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.Future

import play.api.mvc.Action
import scaldi.Injector

class AccountDomainController(implicit inj: Injector) extends AbstractAccountController {

  def create = Action.async { 
    
     implicit request => {
       
       Future.successful(Ok("Added domain"))
    }
  }

}