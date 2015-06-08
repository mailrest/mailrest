/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.Future
import scala.reflect.runtime.universe
import controllers.action.DAuth
import controllers.action.DomainAction
import play.api.mvc.Controller
import scaldi.Injectable
import scaldi.Injector
import services.DomainService
import controllers.action.DAuth

class TemplateController(implicit inj: Injector) extends Controller with Injectable {

  val domainAction = inject [DomainAction]
  
  def create = (domainAction andThen DAuth).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }

}

