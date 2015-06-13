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

trait AccountService {

  def createAccount(user: AccountUser, organization: String, team: String, timezone: String): Future[String]

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
  
  def sendConfirmationEmail(accountId: String, userId: String, email: String): String = {
    
    val cwt = new CallbackWebToken(accountId, userId, CallbackAction.CONFIRM_EMAIL); 
    
    val cwtLink = callbackTokenManager.toJwtLink(cwt, 60 * 24);
    
    println(s"Confirmation email with $cwtLink");
    
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
      
      f1 <- userRepository.saveUser(au.userId(), newPassword, accountId, au.permission()).map { x => x.wasApplied() }
      
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

