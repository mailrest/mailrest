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

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.runtime.universe

import com.mailrest.maildal.model.AccountLog
import com.mailrest.maildal.model.UserInfo

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Writes
import scaldi.Injector
import services.AccountService

class AccountLogController(implicit inj: Injector) extends AbstractAccountController {

  val accountService = inject [AccountService]
  
  implicit val userInfoWrites = new Writes[UserInfo] {
    override def writes(ui: UserInfo): JsValue = {
        Json.obj(
            "userId" -> ui.userId,
            "ip" -> ui.ip,
            "userAgent" -> ui.userAgent
        )
    }
  }
  
  implicit val accountLogWrites = new Writes[AccountLog] {
    override def writes(al: AccountLog): JsValue = {
        Json.obj(
            "accountId" -> al.accountId,
            "eventAt" -> al.eventAt,
            "userInfo" -> al.userInfo,
            "action" -> al.action.name,
            "templateId" -> al.templateId,
            "domainId" -> al.domainId
        )
    }
  }
  
  def find(accId: String, limit: Option[Int]) = readAction(accId).async { 
    
     implicit request => {
    
       val limitVal = limit.getOrElse(10)
       
       accountService.findAccountLogs(accId, limitVal).map { l => Ok(Json.toJson(l)) }
       
    }
  }

}