/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
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

class AccountRequest[A](val accountInfo: Option[AccountInformation], request: Request[A]) extends WrappedRequest[A](request)

class AccountAction(implicit inj: Injector) extends 
    ActionBuilder[AccountRequest] 
    with ActionTransformer[Request, AccountRequest] 
    with Injectable {
  
    val accountTokenManager = inject [TokenManager[AccountWebToken]]
  
    def decodeJwm(jwt: String): Option[AccountInformation] = {
      try {
        val awt = accountTokenManager.fromJwt(jwt);
        Some(new AccountInformation(awt.getAccountId, awt.getUserId, awt.getPermission))
      }
      catch { case e: Exception => None }
    }
    
    def transform[A](request: Request[A]) = Future.successful {
      val accountInfo = request.headers.get("X-Auth-Token").flatMap { x => decodeJwm(x) }
      new AccountRequest(accountInfo, request);
    }
  
}
  
object AccountReadAction extends ActionFilter[AccountRequest] {
  
  def filter[A](input: AccountRequest[A]) = Future.successful {
    input.accountInfo
    match {
      case Some(s) => None
      case None => Some(Results.Forbidden)
    }
  }
  
}

object AccountWriteAction extends ActionFilter[AccountRequest] {
  
  def filter[A](input: AccountRequest[A]) = Future.successful {
    input.accountInfo.filter(x => ((x.userPermission == UserPermission.WRITE) || (x.userPermission == UserPermission.ADMIN)))
    match {
      case Some(s) => None
      case None => Some(Results.Forbidden)
    }
  }
  
}

object AccountAdminAction extends ActionFilter[AccountRequest] {
  
  def filter[A](input: AccountRequest[A]) = Future.successful {
    input.accountInfo.filter(x => x.userPermission == UserPermission.ADMIN)
    match {
      case Some(s) => None
      case None => Some(Results.Forbidden)
    }
  }
  
}

case class AccountInformation(accountId: String, userId: String, userPermission: UserPermission)

