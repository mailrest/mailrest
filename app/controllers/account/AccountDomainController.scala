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
import play.api.mvc.Action
import scaldi.Injector
import play.api.data.Form
import play.api.data.Forms._
import services.AccountService
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.json.JsValue
import utils.ScalaHelper
import com.mailrest.maildal.model.DomainVerificationEvent
import com.mailrest.maildal.model.Domain

class AccountDomainController(implicit inj: Injector) extends AbstractAccountController {

  val accountService = inject [AccountService]

  implicit val domainVerificationEventWrites = new Writes[DomainVerificationEvent] {
      override def writes(dv: DomainVerificationEvent): JsValue = {
          Json.obj(
              "eventAt" -> dv.eventAt,
              "status" -> dv.status.name,
              "message" -> dv.message
          )
      }
  }   
  
  implicit val accountDomainWrites = new Writes[Domain] {
      override def writes(ad: Domain): JsValue = {
          Json.obj(
              "accountId" -> ad.accountId,
              "domainId" -> ad.domainId,
              "createdAt" -> ad.createdAt,
              "domainIdn" -> ad.domainIdn,
              "events" -> Json.arr(ScalaHelper.asSeq(ad.events))
          )
      }
  }    
  
  val newDomainForm = Form(
    mapping (
      "domainIdn" -> nonEmptyText
    )(NewDomainForm.apply)(NewDomainForm.unapply)
  )      
  
  def findAll(accId: String) = readAction(accId).async { 
    
    accountService.findDomains(accId).map(x => Ok(Json.toJson(x)))
    
  }
  
  def create(accId: String) = writeAction(accId).async { 
    
     implicit request => {
       
       val form = newDomainForm.bindFromRequest.get
       
       accountService.addDomain(accId, form.domainIdn).map(x => Ok)
       
    }
     
  }

  def find(accId: String, domIdn: String) = readAction(accId).async { 
    
    accountService.findDomain(accId, domIdn).map { x => {
      
      x match {
        
        case Some(ad) => Ok(Json.toJson(ad))
        case None => NotFound
        
      }
      
    }}
    
  }
  
  def delete(accId: String, domIdn: String) = writeAction(accId).async { 
    
    accountService.deleteDomain(accId, domIdn).map(x => Ok)
    
  }
    
}

case class NewDomainForm(domainIdn: String) 
