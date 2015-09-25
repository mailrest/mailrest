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
package controllers.account

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe
import com.mailrest.maildal.model.User
import play.api.data.Forms._
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import scaldi.Injectable
import scaldi.Injector
import services.AccountService
import play.api.mvc.Result
import play.api.mvc.Results
import services.TokenService
import play.api.data.Form
import play.api.data.Forms._

class TokenController(implicit inj: Injector) extends Controller with Injectable {

  val tokenService = inject [TokenService]
  
  implicit val userWrites = new Writes[User] {
        override def writes(user: User): JsValue = {
            Json.obj(
                "userId" -> user.userId,
                "accountId" -> user.accountId
            )
        }
  }
  
  def findUser(userId: String) = {
    
       tokenService.findUser(userId).map { x => {
         
         x match {
           
           case Some(u) => Ok(Json.toJson(u))
           case None => NotFound
           
         }
         
       } }    
    
       
  }
  
  def find(userId: Option[String]) = Action.async { 
    
    if (userId.isDefined) {
      findUser(userId.get)
    }
    else {
      Future.successful(NoContent)
    }
       
  }
  
  def create = Action.async {
    
    implicit request => {
      
      val form = loginForm.bindFromRequest.get
      
      tokenService.auth(form.userId, form.password).map { x => {
        
        x match {
          
          case Some(t) => Ok(t)
          case None => Forbidden
          
        }
        
      } }
      
    }
    
  }
  
  def extend(jwt: String) = Action.async {
    
    tokenService.extend(jwt).map { x => {
      
      x match {
        
        case Some(t) => Ok(t)
        case None => Forbidden
        
      }
      
      
    } }
    
    
  }
  
  def delete(jwt: String) = Action.async {
    
    tokenService.delete(jwt).map { x => {
      
      if (x) {
        Ok
      }
      else {
        Forbidden
      }
        
    } }
        
    
  }
  
  
  val loginForm = Form(
    mapping(
      "userId" -> nonEmptyText,
      "password" -> nonEmptyText
     )(LoginForm.apply)(LoginForm.unapply)
  )
  
  case class LoginForm(userId: String, password: String)  
  
}