/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package services

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.mailrest.maildal.model.AccountUser
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
import com.mailrest.maildal.repository.DomainOwnerRepository
import com.mailrest.maildal.model.DomainVerificationEvent
import java.util.Date
import com.mailrest.maildal.model.DomainVerificationStatus
import scala.collection.JavaConversions
import com.mailrest.maildal.repository.DomainRepository
import com.mailrest.maildal.model.Domain
import com.mailrest.maildal.repository.UserRef
import com.mailrest.maildal.repository.DomainRef

trait AccountService {

  def createAccount(user: AccountUser, businessName: String, timezone: String): Future[String]
  def findAccount(accId: String): Future[Option[Account]]
  def dropAccount(accId: String): Future[Boolean]
  
  def findAccountLogs(accId: String, limit: Int): Future[Seq[AccountLog]]
  
  def putAccountUser(accId: String, user: AccountUser): Future[Boolean]
  def findAccountUser(id: UserId): Future[Option[AccountUser]]
  def removeUser(id: UserId): Future[Boolean]
  
  def confirmUser(cwt: CallbackWebToken, newPassword: String): Future[Boolean]
  def updatePassword(cwt: CallbackWebToken, newPassword: String): Future[Boolean]
  
  
  def findDomains(accId: String): Future[Seq[Domain]]
  def findDomain(accId: String, domIdn: String): Future[Option[Domain]]
  def addDomain(accId: String, domIdn: String): Future[Boolean]
  def deleteDomain(accId: String, domIdn: String): Future[Boolean]
  
}

class AccountServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends AccountService with Injectable with LazyLogging {
 
  val accountRepository = inject [AccountRepository]
  val domainRepository = inject [DomainRepository]
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
    
    accountRepository.putAccountUser(accId, user).map { x => x.wasApplied() }
    
  }
  
  def findAccountUser(id: UserId): Future[Option[AccountUser]] = {
    
    accountRepository.findAccountUser(id).map(ScalaHelper.asOption)
    
  }
  
  def removeUser(id: UserId): Future[Boolean] = {
  
    for {
      
      f1 <- accountRepository.removeAccountUser(id)
      
      f2 <- userRepository.dropUser(id.userId)
      
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
    
    val id = new UserId(cwt.getAccountId, cwt.getUserId)
    
    accountRepository.findAccountUser(id)
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
      
      f2 <- accountRepository.putAccountUser(accountId, user).map { x => x.wasApplied() }
      
    }
    yield {
      f1 && f2
    }
    
  }

  def updatePassword(cwt: CallbackWebToken, newPassword: String): Future[Boolean] = {
    
    userRepository.updatePassword(cwt.getUserId, newPassword).map { x => x.wasApplied() }
    
  }
  
    
  def findDomains(accId: String): Future[Seq[Domain]] = {
    
    domainRepository.findDomains(accId).map(ScalaHelper.toSeq)
    
  }
  
  
  def findDomain(accId: String, domIdn: String): Future[Option[Domain]] = {
    
    val domainId = com.mailrest.maildal.util.DomainId.INSTANCE.fromDomainIdn(domIdn);
    val id = new DomainId(accId, domainId)    
    
    domainRepository.findDomain(id).map(ScalaHelper.asOption)
    
  }
  
  
  def addDomain(accId: String, domIdn: String): Future[Boolean] = {
    
    val domainId = com.mailrest.maildal.util.DomainId.INSTANCE.fromDomainIdn(domIdn);
    val id = new DomainId(accId, domainId)
    
    domainRepository.addDomain(id, domIdn).map { x => x.wasApplied() }
    
  }  
  
  def deleteDomain(accId: String, domIdn: String): Future[Boolean] = {
      
    val domainId = com.mailrest.maildal.util.DomainId.INSTANCE.fromDomainIdn(domIdn);
    val id = new DomainId(accId, domainId)
    
    domainRepository.dropDomain(id).map { x => x.wasApplied() }      
    
    domainRepository.getVerificationEvents(id).map(ScalaHelper.asOption).flatMap { x => {
      
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
     val id = new DomainId(accId, domainId)
     domainOwnerRepository.dropDomainVerifications(id, list).map { x => x.wasApplied() }    
  }
  
  def deleteAccountDomain(accId: String, domainId: String): Future[Boolean] = {
    
     domainRepository.dropDomain(new DomainId(accId, domainId)).map { x => x.wasApplied() }    
  }
  
}

case class UserId(accountId: String, userId: String) extends UserRef

case class AccountContext(id: UserId, userPermission: UserPermission)

case class AccountUserBean(val userId: String, val email: String, val firstName: String, val lastName: String, val permission: UserPermission, val confirmed: Boolean) extends AccountUser

