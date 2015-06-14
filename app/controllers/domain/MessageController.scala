/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.domain

import scala.reflect.runtime.universe
import play.api.mvc.Controller
import scaldi.Injector
import scala.concurrent.Future

class TemplateController(implicit inj: Injector) extends AbstractDomainController {
  
  def create(domId: String) = domainAction(domId).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }
  
  def find(domId: String, msgId: String) = domainAction(domId).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }
  
  def update(domId: String, msgId: String) = domainAction(domId).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }
  
  def delete(domId: String, msgId: String) = domainAction(domId).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }  

}

