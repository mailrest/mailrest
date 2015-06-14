/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.account

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import controllers.action.AccountAction
import controllers.action.AccountAdminAction
import controllers.action.AccountReadAction
import controllers.action.AccountWriteAction
import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import scaldi.Injectable
import scaldi.Injector
import services.AccountService
import play.api.libs.json.Writes
import play.api.libs.json.JsValue
import com.mailrest.maildal.model.AccountLog
import com.mailrest.maildal.model.UserInfo

class AccountLogController(implicit inj: Injector) extends Controller with Injectable {

  val readAction = inject [AccountAction] andThen AccountReadAction
  val writeAction = inject [AccountAction] andThen AccountWriteAction
  val adminAction = inject [AccountAction] andThen AccountAdminAction
     
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
  
  def find(accId: String, limit: Option[Int]) = readAction.async { 
    
     implicit request => {
    
       val limitVal = limit.getOrElse(10)
       
       accountService.findAccountLogs(accId, limitVal).map { l => Ok(Json.toJson(l)) }
       
    }
  }

}