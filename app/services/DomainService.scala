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
    
    domainRepository.findApiKey(id).map { x => {
      
      if (x.isPresent()) {
        val apiKey = x.get._1
        Some(new DomainContext(id, apiKey))
      }
      else {
        None
      }
      
    }}
    
  }
  
  def lookupDomain(domainId: String): Future[Option[DomainContext]] = {
    
    domainOwnerRepository.findOwner(domainId).flatMap { x=> {
      
      if (x.isPresent()) {
        val accountId = x.get._1
        val id = new DomainId(accountId, domainId)
        lookupDomain(id)
      }
      else {
        Future.successful(None)
      }
      
    }}


  }
  
  
}

case class DomainId(accountId: String, domainId: String) extends DomainRef

case class DomainContext(id: DomainId, apiKey: String) 

