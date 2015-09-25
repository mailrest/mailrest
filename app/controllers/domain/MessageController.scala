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
import java.util.Collections
import scala.collection.JavaConverters
import scala.collection.JavaConversions

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
              "collisionId" -> m.collisionId,
              "from" -> m.fromRecipient,
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
      "collisionId" -> optional(text),    
      "from" -> optional(text), 
      "to" -> nonEmptyText,
      "cc" -> optional(text),
      "bcc" -> optional(text),    
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

       val formEncoded = request.body.asFormUrlEncoded
       val userVariables = formEncoded.fold(Map[String, String]())(parseVariables)
   
       val msg = new NewMessageBean(
           request.domainContext.get.id.accountId,
           request.domainContext.get.id.domainId,
           MessageType.OUTGOING,
           form.deliveryAt.fold(new Date())(f => new Date(f)),
           form.collisionId.getOrElse(""),
           form.from.getOrElse(""),
           form.to,
           form.cc.getOrElse(""),
           form.bcc.getOrElse(""),
           form.templateId.getOrElse(""),
           JavaConversions.mapAsJavaMap(userVariables),
           form.subject.getOrElse(""),
           form.textBody.getOrElse(""),
           form.htmlBody.getOrElse("")
           )
       
      messageService.create(msg).map { x => Ok(x) } 
       
    }
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
  templateId: Option[String],   
  subject: Option[String], textBody: Option[String], htmlBody: Option[String]
) 

