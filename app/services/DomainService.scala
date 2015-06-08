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
  
  def lookupDomain(domain: String, accountId: String): Future[Option[DomainInformation]] = {
    
    domainRepository.findApiKey(domain, accountId).map { x => {
      
      if (x.isPresent()) {
        val apiKey = x.get._1
        Some(new DomainInformation(domain, accountId, apiKey))
      }
      else {
        None
      }
      
    }}
    
  }
  
  def lookupDomain(domain: String): Future[Option[DomainInformation]] = {
    
    domainOwnerRepository.findOwner(domain).flatMap { x=> {
      
      if (x.isPresent()) {
        val accountId = x.get._1
        lookupDomain(domain, accountId)
      }
      else {
        Future.successful(None)
      }
      
    }}


  }
  
}

case class DomainInformation(domain: String, accountId: String, apiKey: String)


