/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import controllers.action.AccountAction
import controllers.action.AccountAdminAction
import controllers.action.AccountReadAction
import controllers.action.AccountWriteAction
import scaldi.Injectable
import scaldi.Injector
import play.api.mvc.Controller
import controllers.action.DomainAuthAction
import controllers.action.DomainAction
import services.DomainService

abstract class AbstractDomainController(implicit inj: Injector) extends Controller with Injectable {

  val domainService = inject [DomainService]
    
  def domainAction(domId: String) = new DomainAction(domId, domainService) andThen DomainAuthAction
    
}