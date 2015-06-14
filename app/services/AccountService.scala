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

trait AccountService {

  def createAccount(user: AccountUser, organization: String, team: String, timezone: String): Future[String]

  def findAccount(accId: String): Future[Option[Account]]
  
  def dropAccount(accId: String): Future[Boolean]
  
  def findAccountLogs(accId: String, limit: Int): Future[Seq[AccountLog]]
  
  def putAccountUser(accId: String, user: AccountUser): Future[Boolean]
  
  def findAccountUser(accId: String, userId: String): Future[Option[AccountUser]]
  
  def removeUser(accId: String, userId: String): Future[Boolean]
  
  def findUser(userId: String): Future[Option[User]]
  
  def confirmUser(cwt: CallbackWebToken, newPassword: String): Future[Boolean]
  
  def updatePassword(cwt: CallbackWebToken, newPassword: String): Future[Boolean]
  
}

class AccountServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends AccountService with Injectable with LazyLogging {
 
  val accountRepository = inject [AccountRepository]
  val accountDomainRepository = inject [AccountDomainRepository]
  val accountLogRepository = inject [AccountLogRepository]
  val userRepository = inject [UserRepository]
  
  val accountTokenManager = inject [TokenManager[AccountWebToken]]
  val callbackTokenManager = inject [TokenManager[CallbackWebToken]]
  
  def createAccount(user: AccountUser, organization: String, team: String, timezone: String): Future[String] = {
    
    logger.info(s"begin createAccount for $organization")
    
    accountRepository.createAccount(user, organization, team, timezone)
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
  
  def findUser(userId: String): Future[Option[User]] = {
    
    userRepository.findUser(userId).map(ScalaHelper.asOption)
    
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
    
    val confirmed = new AccountUserCase(au.userId(), au.email(), au.firstName(), au.lastName(), au.permission(), true)
    
    for {
      
      f1 <- userRepository.saveNewUser(au.userId(), newPassword, accountId, au.permission()).map { x => x.wasApplied() }
      
      f2 <- accountRepository.putAccountUser(accountId, au.userId(), confirmed).map { x => x.wasApplied() }
      
    }
    yield {
      f1 && f2
    }
    
  }

  def updatePassword(cwt: CallbackWebToken, newPassword: String): Future[Boolean] = {
    
    userRepository.updatePassword(cwt.getUserId, newPassword).map { x => x.wasApplied() }
    
  }
  
  case class AccountUserCase(val userId: String, val email: String, val firstName: String, val lastName: String, val permission: UserPermission, val confirmed: Boolean) extends AccountUser

}

