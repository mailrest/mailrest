/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
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