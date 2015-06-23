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

