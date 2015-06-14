/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

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

}

