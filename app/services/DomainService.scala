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
package services

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.runtime.universe
import com.mailrest.maildal.repository.DomainOwnerRepository
import com.mailrest.maildal.repository.DomainRef
import com.mailrest.maildal.repository.DomainRepository
import com.typesafe.scalalogging.slf4j.LazyLogging
import scaldi.Injectable
import scaldi.Injector


trait DomainService {

  def lookupDomain(domain: String): Future[Option[DomainContext]]
  
}

class DomainServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends DomainService with Injectable with LazyLogging {

  val domainRepository = inject [DomainRepository]
  val domainOwnerRepository = inject [DomainOwnerRepository]
  
  def lookupDomain(id: DomainId): Future[Option[DomainContext]] = {
    domainRepository.findApiKey(id).map(x => x.map(y => new DomainContext(id, y._1)))
  }
  
  def lookupDomain(domainId: String): Future[Option[DomainContext]] = {
    
    domainOwnerRepository.findOwner(domainId).flatMap { x=> {
      
      x match {
        
        case Some(y) => {
          val accountId = y._1
          val id = new DomainId(accountId, domainId)
          lookupDomain(id)          
        }
        
        case None => Future.successful(None)
        
      }

    } }
  }
  
  
}

case class DomainId(accountId: String, domainId: String) extends DomainRef

case class DomainContext(id: DomainId, apiKey: String) 

