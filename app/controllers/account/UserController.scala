/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import scaldi.Injectable
import scaldi.Injector
import services.AccountService
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.json.JsValue
import com.mailrest.maildal.model.User

class UserController(implicit inj: Injector) extends Controller with Injectable {

  val accountService = inject [AccountService]
  
  implicit val userWrites = new Writes[User] {
        override def writes(user: User): JsValue = {
            Json.obj(
                "userId" -> user.userId(),
                "accountId" -> user.accountId()
            )
        }
  }
  
  def find(userId: String) = Action.async { 
    
     implicit request => {

       accountService.findUser(userId).map { x => {
         
         x match {
           
           case Some(u) => Ok(Json.toJson(u))
           case None => NotFound
           
         }
         
       } }

    }
  }
  
}