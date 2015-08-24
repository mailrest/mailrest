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

class MessageLogController(implicit inj: Injector) extends AbstractDomainController {
  
  val messageService = inject [MessageService]


  def find(domIdn: String, beforeMls: Option[Long], limit: Option[Int]) = domainAction(domIdn).async { 
     implicit request => {
      Future.successful(Ok)
    }
  }

}
