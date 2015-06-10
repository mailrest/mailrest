/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package services

import com.mailrest.maildal.repository.AbstractRepository
import com.noorq.casser.core.CasserSession
import com.noorq.casser.core.Casser
import scaldi.Injectable
import scaldi.Injector
import com.mailrest.maildal.repository.AccountRepository
import com.mailrest.maildal.repository.AccountDomainRepository
import com.mailrest.maildal.repository.AccountLogRepository
import com.mailrest.maildal.model.AccountUser
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.mailrest.maildal.util.Base58

trait AccountService {

  def createAccount(organization: String, team: String, timezone: String): Future[String]
  
}

class AccountServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends AccountService with Injectable with LazyLogging {
 
  val accountRepository = inject [AccountRepository]
  val accountDomainRepository = inject [AccountDomainRepository]
  val accountLogRepository = inject [AccountLogRepository]
  
  def createAccount(organization: String, team: String, timezone: String): Future[String] = {
    
    logger.info(s"begin createAccount for $organization")
    
    accountRepository.createAccount(organization, team, timezone).map(t => t._2);

  }
    
  
}