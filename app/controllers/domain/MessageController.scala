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
import java.util.Collections
import scala.collection.JavaConverters
import scala.collection.JavaConversions
import com.mailrest.maildal.model.DefaultEnvironments
import play.api.mvc.Result
import controllers.action.DomainRequest
import play.api.mvc.AnyContent
import com.mailrest.maildal.model.MessageRecipient
import com.mailrest.maildal.support.Recipients

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

   implicit val recipientWrites = new Writes[MessageRecipient] {
      override def writes(e: MessageRecipient): JsValue = {
          if (e.recipientName() != null) {
            Json.obj(
                "name" -> e.recipientName(),
                "email" -> e.recipientEmail()
            )
          }
          else {
            Json.obj(
                "email" -> e.recipientEmail()
            )            
          }
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
              "collisionId" -> m.collisionId,
              "from" -> m.fromRecipient,
              "to" -> Json.arr(ScalaHelper.asSeq(m.toRecipients)),
              "cc" -> Json.arr(ScalaHelper.asSeq(m.ccRecipients)),
              "bcc" -> Json.arr(ScalaHelper.asSeq(m.bccRecipients)),
              //"userVariables" -> Json.arr(ScalaHelper.asSeq(m.userVariables.entrySet())),
              "subject" -> m.subject,
              "textBody" -> m.textBody,
              "htmlBody" -> m.htmlBody
          )
      }
  }  
  
  val newMessageForm = Form(
    mapping(
      "deliveryAt" -> optional(longNumber),
      "collisionId" -> optional(text),    
      "from" -> optional(text), 
      "to" -> nonEmptyText,
      "cc" -> optional(text),
      "bcc" -> optional(text),    
      "env" -> optional(text), 
      "templateId" -> optional(text),   
      "subject" -> optional(text), 
      "textBody" -> optional(text), 
      "htmlBody" -> optional(text)
     )(NewMessageForm.apply)(NewMessageForm.unapply)
  )    
  
  def parseVariables(input: Map[String, Seq[String]]): Map[String, String] = {
   
    input
    .filter( k => k._1.startsWith("userVariable.") )
    .map( k => Tuple2(k._1.substring(13), k._2.head))
    
  }
  
  def create(domIdn: String) = domainAction(domIdn).async { 
     implicit request => {
      
       val form = newMessageForm.bindFromRequest.get  

       if (form.templateId.isDefined) {
         val formEncoded = request.body.asFormUrlEncoded
         val userVariables = formEncoded.fold(Map[String, String]())(parseVariables)
         
         // TODO: request template, replace fields in message

         sendSimpleMessage(form)
       }
       else {
         sendSimpleMessage(form)
       }
       
    }
  }
  
  def correctDate(timestamp: Option[Long]): Date = {
    if (timestamp.isDefined) {
      new Date(Math.max(timestamp.get, java.lang.System.currentTimeMillis()))
    }
    else {
      new Date()
    }
  }
  
  def sendSimpleMessage(form: NewMessageForm)(implicit request: DomainRequest[AnyContent]) : Future[Result] = {
    
     val msg = new NewMessageBean(
     request.domainContext.get.id.accountId,
     request.domainContext.get.id.domainId,
     MessageType.OUTGOING,
     correctDate(form.deliveryAt),
     form.collisionId.getOrElse(null),     
     Recipients.INSTANCE.parseSingle(form.from.getOrElse("")),
     Recipients.INSTANCE.parseMulti(form.to, false),
     Recipients.INSTANCE.parseMulti(form.cc.getOrElse(null), false),
     Recipients.INSTANCE.parseMulti(form.bcc.getOrElse(null), false),
     form.subject.getOrElse(""),
     form.textBody.getOrElse(""),
     form.htmlBody.getOrElse(null)
     )
     
    messageService.create(msg).map { x => Ok(x) } 
  }
  
  def find(domIdn: String, msgId: String) = domainAction(domIdn).async { 
     implicit request => {
      
       messageService.find(msgId, request.domainContext.get.id).map { x => Ok(Json.toJson(x)) }
       
    }
  }
  
  def delete(domIdn: String, msgId: String) = domainAction(domIdn).async { 
     implicit request => {
      
       messageService.delete(msgId, request.domainContext.get.id).map { x => Ok }
       
    }
  }  

}

case class UserVariableForm(name: String, value: String)

case class NewMessageForm(
  deliveryAt: Option[Long], collisionId: Option[String],    
  from: Option[String], to: String, cc: Option[String], bcc: Option[String],    
  env: Option[String], templateId: Option[String],   
  subject: Option[String], textBody: Option[String], htmlBody: Option[String]
) 

