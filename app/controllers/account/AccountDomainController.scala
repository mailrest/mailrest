/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
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
