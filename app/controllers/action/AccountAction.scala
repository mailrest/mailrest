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
package controllers.action

import scala.concurrent.Future
import play.api.mvc.ActionBuilder
import play.api.mvc.ActionFilter
import play.api.mvc.ActionTransformer
import play.api.mvc.Request
import play.api.mvc.WrappedRequest
import play.api.mvc.Results
import scala.annotation.implicitNotFound
import services.AccountService
import scaldi.Injectable
import scaldi.Injector
import com.mailrest.maildal.model.UserPermission
import com.mailrest.maildal.secur.TokenManager
import views.html.helper.input
import com.mailrest.maildal.secur.AccountWebToken
import com.mailrest.maildal.repository.UserRef
import services.AccountContext
import services.UserId

class AccountRequest[A](val accountContext: Option[AccountContext], request: Request[A]) extends WrappedRequest[A](request)

class AccountAction(implicit inj: Injector) extends 
    ActionBuilder[AccountRequest] 
    with ActionTransformer[Request, AccountRequest] 
    with Injectable {
  
    val accountTokenManager = inject [TokenManager[AccountWebToken]]
  
    def decodeJwm(jwt: String): Option[AccountContext] = {
      try {
        val awt = accountTokenManager.fromJwt(jwt);
        val id = new UserId(awt.getAccountId, awt.getUserId)
        Some(new AccountContext(id, awt.getPermission))
      }
      catch { case e: Exception => None }
    }
    
    def transform[A](request: Request[A]) = Future.successful {
      val accountInfo = request.headers.get("X-Auth-Token").flatMap { x => decodeJwm(x) }
      new AccountRequest(accountInfo, request);
    }
  
}

class AccountReadAction(accId: String) extends ActionFilter[AccountRequest] {
  
  def filter[A](input: AccountRequest[A]) = Future.successful {
    input.accountContext.filter { x => x.id.accountId == accId }
    match {
      case Some(s) => None
      case None => Some(Results.Forbidden)
    }
  }
  
}

class AccountWriteAction(accId: String) extends ActionFilter[AccountRequest] {
  
  def filter[A](input: AccountRequest[A]) = Future.successful {
    input.accountContext.filter { x => x.id.accountId == accId }
    .filter(x => x.userPermission != UserPermission.READ_ONLY)
    match {
      case Some(s) => None
      case None => Some(Results.Forbidden)
    }
  }
  
}

class AccountAdminAction(accId: String) extends ActionFilter[AccountRequest] {
  
  def filter[A](input: AccountRequest[A]) = Future.successful {
    input.accountContext.filter { x => x.id.accountId == accId }
    .filter(x => x.userPermission == UserPermission.ADMIN)
    match {
      case Some(s) => None
      case None => Some(Results.Forbidden)
    }
  }
  
}

