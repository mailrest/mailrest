package services

import com.typesafe.scalalogging.slf4j.LazyLogging
import scaldi.Injectable
import com.mailrest.maildal.repository.AccountRepository
import scala.concurrent.ExecutionContext
import scaldi.Injector
import com.mailrest.maildal.repository.DomainRepository
import com.mailrest.maildal.repository.DomainOwnerRepository
import scala.concurrent.Future

trait DomainService {

  def lookupDomain(domain: String): Future[Option[DomainInformation]]
  
}

class DomainServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends DomainService with Injectable with LazyLogging {
 
  val domainRepository = inject [DomainRepository]
  val domainOwnerRepository = inject [DomainOwnerRepository]
  
  def lookupDomain(domainId: String, accountId: String): Future[Option[DomainInformation]] = {
    
    domainRepository.findApiKey(domainId, accountId).map { x => {
      
      if (x.isPresent()) {
        val apiKey = x.get._1
        Some(new DomainInformation(domainId, accountId, apiKey))
      }
      else {
        None
      }
      
    }}
    
  }
  
  def lookupDomain(domainId: String): Future[Option[DomainInformation]] = {
    
    domainOwnerRepository.findOwner(domainId).flatMap { x=> {
      
      if (x.isPresent()) {
        val accountId = x.get._1
        lookupDomain(domainId, accountId)
      }
      else {
        Future.successful(None)
      }
      
    }}


  }
  
}

case class DomainInformation(domainId: String, accountId: String, apiKey: String)


