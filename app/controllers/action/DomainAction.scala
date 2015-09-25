/*
 *      Copyright (C) 2015 Noorq, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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
import services.DomainService
import services.DomainId
import services.DomainContext


class DomainRequest[A](val domainContext: Option[DomainContext], request: Request[A]) extends WrappedRequest[A](request)

class DomainAction(domIdn: String, domainService: DomainService) extends
    ActionBuilder[DomainRequest] 
    with ActionTransformer[Request, DomainRequest] {
  
  def transform[A](request: Request[A]) = {
    
    val domainId = com.mailrest.maildal.util.DomainId.INSTANCE.fromDomainIdn(domIdn);
    
    domainService.lookupDomain(domainId).map { v => new DomainRequest[A](v, request) }

  }
  
}
  
object DomainAuthAction extends ActionFilter[DomainRequest] {
  
   def authByBasic[A](input: DomainRequest[A], domainContext: DomainContext): Option[Result] = {
      BasicAuthHelper.getCredentials(input).filter( x => x._1 == "api" && x._2 == domainContext.apiKey)
      match {
        case Some(s) => None
        case None => Some(BasicAuthHelper.requestAuth)
      }
   }
  
   def authByToken[A](input: DomainRequest[A], domainContext: DomainContext): Option[Result] = {
      input.headers.get("X-Auth-Token").filter { x => x == domainContext.apiKey } 
      match {
        case Some(s) => None
        case None => authByBasic(input, domainContext)
      }
  }

  
  def filter[A](input: DomainRequest[A]): Future[Option[Result]] = Future.successful {
    
    input.domainContext match {
      case Some(di) => authByToken(input, di)
      case None => Some(Results.NotFound)
    }
    
  }
  
}


