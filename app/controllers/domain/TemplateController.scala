/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.Future

import scaldi.Injector

class MessageController(implicit inj: Injector) extends AbstractDomainController {

  def create(domId: String) = domainAction(domId).async { 
     implicit request => {
      
      Future.successful { Ok("Created template: ") }
    }
  }

}

