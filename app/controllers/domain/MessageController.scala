/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.domain

import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.runtime.universe
import play.api.mvc.Controller
import scaldi.Injector
import scala.concurrent.Future
import services.MessageService

class MessageController(implicit inj: Injector) extends AbstractDomainController {
  
  val messageService = inject [MessageService]

  def create(domIdn: String) = domainAction(domIdn).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }
  
  def find(domIdn: String, msgId: String) = domainAction(domIdn).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }
  
  def update(domIdn: String, msgId: String) = domainAction(domIdn).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }
  
  def delete(domIdn: String, msgId: String) = domainAction(domIdn).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }  

}

