/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers.domain

import java.util.Date
import java.util.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe
import com.mailrest.maildal.model.Message
import com.mailrest.maildal.model.MessageType
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.mvc.Controller
import scaldi.Injector
import services.MessageService
import services.NewMessageBean
import utils.ScalaHelper

class MessageController(implicit inj: Injector) extends AbstractDomainController {
  
  val messageService = inject [MessageService]

    implicit val userVariableWrites = new Writes[java.util.Map.Entry[String, String]] {
      override def writes(e: java.util.Map.Entry[String, String]): JsValue = {
          Json.obj(
              "name" -> e.getKey,
              "value" -> e.getValue
          )
      }
  }  
  
  implicit val messageWrites = new Writes[Message] {
      override def writes(m: Message): JsValue = {
          Json.obj(
              "messageId" -> m.messageId,
              "createdAt" -> m.createdAt.getTime,
              "deliveryAt" -> m.deliveryAt.getTime,
              "messageType" -> m.messageType.name,
              "accountId" -> m.accountId,
              "domainId" -> m.domainId,
              "publicId" -> m.publicId,
              "from" -> m.fromRecipients,
              "to" -> m.toRecipients,
              "cc" -> m.ccRecipients,
              "bcc" -> m.bccRecipients,
              "templateId" -> m.templateId,
              "userVariables" -> Json.arr(ScalaHelper.asSeq(m.userVariables.entrySet())),
              "subject" -> m.subject,
              "textBody" -> m.textBody,
              "htmlBody" -> m.htmlBody
          )
      }
  }  
  
  val newMessageForm = Form(
    mapping(
      "deliveryAt" -> optional(longNumber),
      "publicId" -> optional(text),    
      "from" -> optional(text), 
      "to" -> nonEmptyText,
      "cc" -> optional(text),
      "bcc" -> optional(text),    
      "templateId" -> optional(text),
      "userVariables" -> optional(text),    
      "subject" -> optional(text), 
      "textBody" -> optional(text), 
      "htmlBody" -> optional(text)
     )(NewMessageForm.apply)(NewMessageForm.unapply)
  )    
  
  def parseMap(input: String): java.util.Map[String, String] = {
     new HashMap[String, String]()
  }
  
  def create(domIdn: String) = domainAction(domIdn).async { 
     implicit request => {
      
       val form = newMessageForm.bindFromRequest.get  

       val msg = new NewMessageBean(
           request.domainInfo.get.domainId,
           request.domainInfo.get.accountId,
           MessageType.OUTGOING,
           form.deliveryAt.fold(new Date())(f => new Date(f)),
           form.publicId.getOrElse(""),
           form.from.getOrElse(""),
           form.to,
           form.cc.getOrElse(""),
           form.bcc.getOrElse(""),
           form.templateId.getOrElse(""),
           parseMap(form.userVariables.getOrElse("")),
           form.subject.getOrElse(""),
           form.textBody.getOrElse(""),
           form.htmlBody.getOrElse("")
           )
       
      messageService.create(msg).map { x => Ok(x) } 
       
    }
  }
  
  def find(domIdn: String, msgId: String) = domainAction(domIdn).async { 
     implicit request => {
      
       messageService.find(msgId, 
           request.domainInfo.get.domainId,
           request.domainInfo.get.accountId
           ).map { x => Ok(Json.toJson(x)) }
       
    }
  }
  
  def delete(domIdn: String, msgId: String) = domainAction(domIdn).async { 
     implicit request => {
      
       messageService.delete(msgId,
           request.domainInfo.get.domainId,
           request.domainInfo.get.accountId
           ).map { x => Ok }
       
    }
  }  

}


case class NewMessageForm(
  deliveryAt: Option[Long], publicId: Option[String],    
  from: Option[String], to: String, cc: Option[String], bcc: Option[String],    
  templateId: Option[String], userVariables: Option[String],    
  subject: Option[String], textBody: Option[String], htmlBody: Option[String]
) 

