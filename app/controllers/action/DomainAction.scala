/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.action

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe

import play.api.mvc.ActionBuilder
import play.api.mvc.ActionFilter
import play.api.mvc.ActionTransformer
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results
import play.api.mvc.WrappedRequest
import scaldi.Injectable
import scaldi.Injector
import services.DomainInformation
import services.DomainService

class DomainRequest[A](val domainInfo: Option[DomainInformation], request: Request[A]) extends WrappedRequest[A](request)

class DomainAction(implicit inj: Injector) extends
    ActionBuilder[DomainRequest] 
    with ActionTransformer[Request, DomainRequest]
    with Injectable {
  
  val domainService = inject [DomainService]
  
  def transform[A](request: Request[A]) = {
    
    val domainId = request.domain.toLowerCase();
    
    domainService.lookupDomain(domainId).map { v => new DomainRequest[A](v, request) }

  }
  
}
  
object DomainAuthAction extends ActionFilter[DomainRequest] {
  
   def auth[A](input: DomainRequest[A], domainInfo: DomainInformation): Option[Result] = {
      input.headers.get("X-Auth-Token").filter { x => x == domainInfo.apiKey } 
      match {
        case Some(s) => None
        case None => Some(Results.Forbidden)
      }
  }

  
  def filter[A](input: DomainRequest[A]): Future[Option[Result]] = Future.successful {
    
    input.domainInfo match {
      case Some(di) => auth(input, di)
      case None => Some(Results.Forbidden)
    }
    
  }
  
}

