/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package services

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.mailrest.maildal.model.AccountUser
import com.mailrest.maildal.repository.AccountDomainRepository
import com.mailrest.maildal.repository.AccountLogRepository
import com.mailrest.maildal.repository.AccountRepository
import com.mailrest.maildal.repository.UserRepository
import com.mailrest.maildal.secur.AccountWebToken
import com.mailrest.maildal.secur.CallbackWebToken
import com.mailrest.maildal.secur.TokenManager
import com.typesafe.scalalogging.slf4j.LazyLogging
import scaldi.Injectable
import scaldi.Injector
import com.mailrest.maildal.model.UserPermission
import com.mailrest.maildal.model.CallbackAction
import com.mailrest.maildal.model.User
import utils.ScalaHelper
import com.mailrest.maildal.model.Account
import com.mailrest.maildal.model.AccountLog
import com.mailrest.maildal.util.DomainId
import com.mailrest.maildal.model.AccountDomain
import com.mailrest.maildal.repository.DomainOwnerRepository
import com.mailrest.maildal.model.DomainVerificationEvent
import java.util.Date
import com.mailrest.maildal.model.DomainVerificationStatus
import scala.collection.JavaConversions

trait AccountService {

  def createAccount(user: AccountUser, businessName: String, timezone: String): Future[String]
  def findAccount(accId: String): Future[Option[Account]]
  def dropAccount(accId: String): Future[Boolean]
  
  def findAccountLogs(accId: String, limit: Int): Future[Seq[AccountLog]]
  
  def putAccountUser(accId: String, user: AccountUser): Future[Boolean]
  def findAccountUser(accId: String, userId: String): Future[Option[AccountUser]]
  def removeUser(accId: String, userId: String): Future[Boolean]
  
  def confirmUser(cwt: CallbackWebToken, newPassword: String): Future[Boolean]
  def updatePassword(cwt: CallbackWebToken, newPassword: String): Future[Boolean]
  
  
  def findDomains(accId: String): Future[Seq[AccountDomain]]
  def findDomain(accId: String, domIdn: String): Future[Option[AccountDomain]]
  def addDomain(accId: String, domIdn: String): Future[Boolean]
  def deleteDomain(accId: String, domIdn: String): Future[Boolean]
  
}

class AccountServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends AccountService with Injectable with LazyLogging {
 
  val accountRepository = inject [AccountRepository]
  val accountDomainRepository = inject [AccountDomainRepository]
  val accountLogRepository = inject [AccountLogRepository]
  val userRepository = inject [UserRepository]
  
  val domainOwnerRepository = inject [DomainOwnerRepository]
  
  val accountTokenManager = inject [TokenManager[AccountWebToken]]
  val callbackTokenManager = inject [TokenManager[CallbackWebToken]]
  
  def createAccount(user: AccountUser, businessName: String, timezone: String): Future[String] = {
    
    logger.info(s"createAccount for $businessName")
    
    accountRepository.createAccount(user, businessName, timezone)
    .map(t => t._2)
    .map(accId => sendConfirmationEmail(accId, user.userId(), user.email()))

  }
  
  def findAccount(accId: String): Future[Option[Account]] = {
    
    accountRepository.findAccount(accId).map(ScalaHelper.asOption)
    
  }
  
  def dropAccount(accId: String): Future[Boolean] = {
    
    accountRepository.dropAccount(accId).map { x => x.wasApplied() }
    
  }
  
  def findAccountLogs(accId: String, limit: Int): Future[Seq[AccountLog]] = {
    
    accountLogRepository.getAccountLogs(accId, limit).map(ScalaHelper.toSeq)
    
  }
  
  def putAccountUser(accId: String, user: AccountUser): Future[Boolean] = {
    
    accountRepository.putAccountUser(accId, user.userId, user).map { x => x.wasApplied() }
    
  }
  
  def findAccountUser(accId: String, userId: String): Future[Option[AccountUser]] = {
    
    accountRepository.findAccountUser(accId, userId).map(ScalaHelper.asOption)
    
  }
  
  def removeUser(accId: String, userId: String): Future[Boolean] = {
  
    for {
      
      f1 <- accountRepository.removeAccountUser(accId, userId)
      
      f2 <- userRepository.dropUser(userId)
      
    }
    yield {
     
      f1.wasApplied() || f2.wasApplied()
      
    }
    
  }
  
  
  def sendConfirmationEmail(accountId: String, userId: String, email: String): String = {
    
    val cwt = new CallbackWebToken(accountId, userId, CallbackAction.CONFIRM_EMAIL); 
    
    val cwtLink = callbackTokenManager.toJwtLink(cwt, 60 * 24);
    
    println(s"Confirmation email with link $cwtLink");
    
    return accountId
  }
  
  def confirmUser(cwt: CallbackWebToken, newPassword: String): Future[Boolean] = {
    
    accountRepository.findAccountUser(cwt.getAccountId, cwt.getUserId)
    .flatMap {
      oau => {
  
          if (oau.isPresent()) {
            saveConfirmedUser(cwt.getAccountId, oau.get, newPassword)
          }
          else {
            Future.successful(false)
          }
        
      }
      
    }
    
  }
  
  def saveConfirmedUser(accountId: String, au: AccountUser, newPassword: String): Future[Boolean] = {
    
    val user = new AccountUserBean(au.userId(), au.email(), au.firstName(), au.lastName(), au.permission(), true)
    
    for {
      
      f1 <- userRepository.saveNewUser(au.userId(), newPassword, accountId, au.permission()).map { x => x.wasApplied() }
      
      f2 <- accountRepository.putAccountUser(accountId, au.userId(), user).map { x => x.wasApplied() }
      
    }
    yield {
      f1 && f2
    }
    
  }

  def updatePassword(cwt: CallbackWebToken, newPassword: String): Future[Boolean] = {
    
    userRepository.updatePassword(cwt.getUserId, newPassword).map { x => x.wasApplied() }
    
  }
  
    
  def findDomains(accId: String): Future[Seq[AccountDomain]] = {
    
    accountDomainRepository.findDomains(accId).map(ScalaHelper.toSeq)
    
  }
  
  
  def findDomain(accId: String, domIdn: String): Future[Option[AccountDomain]] = {
    
    val domainId = DomainId.INSTANCE.fromDomainIdn(domIdn);
        
    accountDomainRepository.findAccountDomain(accId, domainId).map(ScalaHelper.asOption)
    
  }
  
  
  def addDomain(accId: String, domIdn: String): Future[Boolean] = {
    
    val domainId = DomainId.INSTANCE.fromDomainIdn(domIdn);
    
    accountDomainRepository.addAccountDomain(accId, domainId, domIdn).map { x => x.wasApplied() }
    
  }  
  
  def deleteDomain(accId: String, domIdn: String): Future[Boolean] = {
      
    val domainId = DomainId.INSTANCE.fromDomainIdn(domIdn);
    
    accountDomainRepository.dropAccountDomain(accId, domainId).map { x => x.wasApplied() }      
    
    accountDomainRepository.getVerificationEvents(accId, domainId).map(ScalaHelper.asOption).flatMap { x => {
      
      x match {
        
        case Some(events) => deleteAccountDomainWithOwnership(accId, domainId, events)
        case None => deleteAccountDomain(accId, domainId)
      }
      
    } }
    
      
  }
  
  def deleteAccountDomainWithOwnership(accId: String, domainId: String, events: java.util.List[DomainVerificationEvent]): Future[Boolean] = {
    
    for {
       f1 <- deleteDomainOwnership(accId, domainId, getVerifiedAt(events))
       f2 <- deleteAccountDomain(accId, domainId)
    }
    yield {
       f1 || f2
    }
    
  }
  
  def getVerifiedAt(events: java.util.List[DomainVerificationEvent]): Seq[Date] = {

    val buffer = JavaConversions.asScalaBuffer(events)

    buffer.filter { x => x.status == DomainVerificationStatus.VERIFIED }.map { x => x.eventAt }
    
  }
  
  def deleteDomainOwnership(accId: String, domainId: String, verifiedAt: Seq[Date]): Future[Boolean] = {
    
     val list = JavaConversions.seqAsJavaList(verifiedAt)
    
     domainOwnerRepository.dropDomainVerifications(accId, domainId, list).map { x => x.wasApplied() }    
  }
  
  def deleteAccountDomain(accId: String, domainId: String): Future[Boolean] = {
    
     accountDomainRepository.dropAccountDomain(accId, domainId).map { x => x.wasApplied() }    
  }
  
}

case class AccountUserBean(val userId: String, val email: String, val firstName: String, val lastName: String, val permission: UserPermission, val confirmed: Boolean) extends AccountUser



