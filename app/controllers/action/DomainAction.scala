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
import com.mailrest.maildal.util.DomainId

class DomainRequest[A](val domainInfo: Option[DomainInformation], request: Request[A]) extends WrappedRequest[A](request)

class DomainAction(domId: String, domainService: DomainService) extends
    ActionBuilder[DomainRequest] 
    with ActionTransformer[Request, DomainRequest] {
  
  def transform[A](request: Request[A]) = {
    
    val domainId = DomainId.INSTANCE.fromDomain(domId);
    
    domainService.lookupDomain(domainId).map { v => new DomainRequest[A](v, request) }

  }
  
}
  
object DomainAuthAction extends ActionFilter[DomainRequest] {
  
   def authByBasic[A](input: DomainRequest[A], domainInfo: DomainInformation): Option[Result] = {
      BasicAuthHelper.getCredentials(input).filter( x => x._1 == "api" && x._2 == domainInfo.apiKey)
      match {
        case Some(s) => None
        case None => Some(BasicAuthHelper.requestAuth)
      }
   }
  
   def authByToken[A](input: DomainRequest[A], domainInfo: DomainInformation): Option[Result] = {
      input.headers.get("X-Auth-Token").filter { x => x == domainInfo.apiKey } 
      match {
        case Some(s) => None
        case None => authByBasic(input, domainInfo)
      }
  }

  
  def filter[A](input: DomainRequest[A]): Future[Option[Result]] = Future.successful {
    
    input.domainInfo match {
      case Some(di) => authByToken(input, di)
      case None => Some(Results.NotFound)
    }
    
  }
  
}

