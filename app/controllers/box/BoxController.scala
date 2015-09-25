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
package controllers.box

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
import controllers.domain.AbstractDomainController

class BoxController(implicit inj: Injector) extends AbstractDomainController {
  
  val messageService = inject [MessageService]

  def findAll(domIdn: String) = domainAction(domIdn).async { 
     implicit request => {
      Future.successful(Ok)
    }
  }
  
  def create(domIdn: String) = domainAction(domIdn).async { 
     implicit request => {
      Future.successful(Ok)
    }
  }

  def find(domIdn: String, boxId: String) = domainAction(domIdn).async { 
     implicit request => {
      Future.successful(Ok)
    }
  }
  
  def delete(domIdn: String, boxId: String) = domainAction(domIdn).async { 
     implicit request => {
      Future.successful(Ok)
    }
  }  

}
