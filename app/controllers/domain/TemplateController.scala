/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.Future
import scala.reflect.runtime.universe
import controllers.action.DomainAction
import play.api.mvc.Controller
import scaldi.Injectable
import scaldi.Injector
import services.DomainService
import controllers.action.DomainAuthAction

class MessageController(implicit inj: Injector) extends Controller with Injectable {

  val action = inject [DomainAction] andThen DomainAuthAction
  
  def create = action.async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }

}
