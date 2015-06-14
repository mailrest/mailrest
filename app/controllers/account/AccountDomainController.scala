/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.Future

import play.api.mvc.Action
import scaldi.Injector

class AccountDomainController(implicit inj: Injector) extends AbstractAccountController {

  def create(accId: String) = writeAction(accId).async { 
    
     implicit request => {
       
       Future.successful(Ok("Added domain"))
    }
  }

  def find(accId: String, domId: String) = readAction(accId).async { 
    
     implicit request => {
       
       Future.successful(Ok("Added domain"))
    }
  }
  
  def update(accId: String, domId: String) = writeAction(accId).async { 
    
     implicit request => {
       
       Future.successful(Ok("Added domain"))
    }
  }
  
  def delete(accId: String, domId: String) = writeAction(accId).async { 
    
     implicit request => {
       
       Future.successful(Ok("Added domain"))
    }
  }
    
}