/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.domain

import scala.concurrent.Future

import scaldi.Injector


class MessageController(implicit inj: Injector) extends AbstractDomainController {

  def create(domId: String) = domainAction(domId).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }
  
  def find(domId: String, tplId: String) = domainAction(domId).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }
  
  def update(domId: String, tplId: String) = domainAction(domId).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }
  
  def delete(domId: String, tplId: String) = domainAction(domId).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }  

}

