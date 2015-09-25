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

import scala.concurrent.ExecutionContext.Implicits.global
import com.mailrest.maildal.model.Template
import com.mailrest.maildal.model.TemplateEngineType
import controllers.action.DomainRequest
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.mvc.AnyContent
import scaldi.Injector
import services.TemplateBean
import services.TemplateId
import services.TemplateService
import services.TemplateInfo

class TemplateController(implicit inj: Injector) extends AbstractDomainController {

  val templateService = inject [TemplateService]
  
  implicit val templateWrites = new Writes[TemplateInfo] {
      override def writes(t: TemplateInfo): JsValue = {
          Json.obj(
              "displayName" -> t.template.displayName,
              "description" -> t.template.description,
              "engine" -> t.template.engine.name,
              "fromRecipients" -> t.template.fromRecipients,
              "bccRecipients" -> t.template.bccRecipients,
              "subject" -> t.template.subject,
              "textBody" -> t.template.textBody,
              "htmlBody" -> t.template.htmlBody,
              "deployedAt" -> t.deployedAt
          )
      }
  }  
  
  val templateMapping = mapping(
      "name" -> optional(text),
      "description" -> optional(text),
      "engine" -> nonEmptyText,
      "fromRecipients" -> optional(text),
      "bccRecipients" -> optional(text),
      "subject" -> optional(text),
      "textBody" -> optional(text),
      "htmlBody" -> optional(text)
     )(TemplateForm.apply)(TemplateForm.unapply)
  
  val newTemplateForm = Form(
    mapping(
      "templateId" -> nonEmptyText,
      "env" -> nonEmptyText,
      "template" -> templateMapping
     )(NewTemplateForm.apply)(NewTemplateForm.unapply)
  )  

  val templateForm = Form(templateMapping)  

  def makeId(request: DomainRequest[AnyContent], templateId: String, env: String): TemplateId = {
     new TemplateId(
          request.domainContext.get.id.accountId,
          request.domainContext.get.id.domainId,
          templateId,
          env
     ) 
  }
  
  def makeBean(form: TemplateForm): TemplateBean = {
    
     new TemplateBean(
          form.name.getOrElse(""),
          form.description.getOrElse(""),
          TemplateEngineType.valueOf(form.engine),
          form.fromRecipients.getOrElse(""),
          form.bccRecipients.getOrElse(""),
          form.subject.getOrElse(""),
          form.textBody.getOrElse(""),
          form.htmlBody.getOrElse("")
      )
      
  }
  
  def create(domIdn: String) = domainAction(domIdn).async { 
     implicit request => {
      
      val form = newTemplateForm.bindFromRequest.get 

      templateService.update(
          makeId(request, form.templateId, form.env), 
          makeBean(form.template)
          ).map { x => Ok }
      
    }
  }
  
  def find(domIdn: String, tplId: String, env: String) = domainAction(domIdn).async {
    
     implicit request => {
      
      templateService.find(makeId(request, tplId, env)).map { x => {
            
            x match {
              
              case Some(t) => Ok(Json.toJson(t))
              case None => NotFound
              
            }
            
          } }
       
    }
  }
  
  def update(domIdn: String, tplId: String, env: String) = domainAction(domIdn).async { 
     implicit request => {
       
      val form = templateForm.bindFromRequest.get 
      
      templateService.update(
          makeId(request, tplId, env), 
          makeBean(form)
          ).map { x => Ok }
    }
  }
  
  def delete(domIdn: String, tplId: String, env: String, deployedAt: Option[Long]) = domainAction(domIdn).async { 
     implicit request => {
      
      templateService.delete(
          makeId(request, tplId, env), 
          deployedAt.getOrElse(0)
          ).map { x => Ok }          
       
    }
  }  

}

case class NewTemplateForm(templateId: String, env: String, template: TemplateForm)

case class TemplateForm(
    
name: Option[String], description: Option[String], engine: String, 
fromRecipients: Option[String], bccRecipients: Option[String],
subject: Option[String], textBody: Option[String], htmlBody: Option[String]

) 


