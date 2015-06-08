package controllers

import scala.concurrent.Future
import play.api.mvc.ActionBuilder
import play.api.mvc.ActionFilter
import play.api.mvc.ActionTransformer
import play.api.mvc.Request
import play.api.mvc.WrappedRequest
import play.api.mvc.Results

class AccountRequest[A](val accountId: String, val domainId: String, val apiKey: String, request: Request[A]) extends WrappedRequest[A](request)

object AccountAction extends
    ActionBuilder[AccountRequest] 
    with ActionTransformer[Request, AccountRequest] {
  
  def transform[A](request: Request[A]) = Future.successful {
    
    new AccountRequest("123", "mailrest", "system", request)
    
  }
  
}
  
object AuthIt extends ActionFilter[AccountRequest] {
  
  def filter[A](input: AccountRequest[A]) = Future.successful {
    input.headers.get("X-Auth-Token").filter { x => x == input.apiKey } 
    match {
      case Some(s) => None
      case None => Some(Results.Forbidden)
    }
  }
  
}

