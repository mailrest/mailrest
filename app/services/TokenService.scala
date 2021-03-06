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

import scala.concurrent.Future
import com.mailrest.maildal.model.User
import com.typesafe.scalalogging.slf4j.LazyLogging
import scaldi.Injectable
import scala.concurrent.ExecutionContext
import scaldi.Injector
import com.mailrest.maildal.repository.UserRepository
import utils.ScalaHelper
import com.mailrest.maildal.secur.PasswordHash
import com.mailrest.maildal.secur.TokenManager
import com.mailrest.maildal.secur.AccountWebToken

trait TokenService {

    def findUser(userId: String): Future[Option[User]]
    
    def auth(userId: String, password: String): Future[Option[String]]
  
    def extend(token: String): Future[Option[String]]
    
    def delete(token: String): Future[Boolean]
    
}


class TokenServiceImpl (implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends TokenService with Injectable with LazyLogging  {

  val userRepository = inject [UserRepository]
  val accountTokenManager = inject [TokenManager[AccountWebToken]]

  def findUser(userId: String): Future[Option[User]] = {
    
    userRepository.findUser(userId)
    
  }
  
  def decodeJwt(jwt: String): Option[AccountWebToken] = {
      try {
        Some(accountTokenManager.fromJwt(jwt))
      }
      catch { case e: Exception => None }
  }
  
  def extendToken(awt: AccountWebToken): String = {

    val newAwt = new AccountWebToken(awt.getAccountId, awt.getUserId, awt.getPermission) 
    accountTokenManager.toJwt(newAwt, 20)
    
  }
  
  def createToken(user: User): Option[String] = {
    
    val awt = new AccountWebToken(user.accountId, user.userId, user.permission) 
    Some(accountTokenManager.toJwt(awt, 20))

  }
  
  def auth(userId: String, password: String): Future[Option[String]] = {
    
    val passwordHash = PasswordHash.INSTANCE.calculate(password)
    
    findUser(userId).filter { x => x.isDefined && x.get.passwordHash == passwordHash }.map { x => {
      
      x match {
        
        case Some(u) => createToken(u)
        case None => None
        
      }
      
    } }
    
  }
  
  def extend(jwt: String): Future[Option[String]] = {
    
    Future.successful {
    
       decodeJwt(jwt).map(x => extendToken(x))
    
    }
    
  }
    
  def delete(jwt: String): Future[Boolean] = {
    
    Future.successful {
      
      true
      
    }
    
  } 
  
}