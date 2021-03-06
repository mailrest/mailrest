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
package services

import com.typesafe.scalalogging.slf4j.LazyLogging
import scaldi.Injectable
import scala.concurrent.ExecutionContext
import scaldi.Injector
import com.mailrest.maildal.repository.TemplateRepository
import com.mailrest.maildal.model.Template
import com.mailrest.maildal.model.TemplateEngineType
import scala.concurrent.Future
import utils.ScalaHelper
import com.mailrest.maildal.model.DefaultEnvironments
import java.util.Optional
import com.noorq.casser.support.Fun
import java.util.Date
import com.mailrest.maildal.repository.TemplateRef

trait TemplateService {

  def update(id: TemplateId, template: TemplateBean): Future[Boolean]
  
  def find(id: TemplateId): Future[Option[TemplateInfo]]
  
  def delete(id: TemplateId, deployedAt: Long): Future[Boolean]
  
}

class TemplateServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends TemplateService with Injectable with LazyLogging {
 
  val templateRepository = inject [TemplateRepository]
  
  def update(id: TemplateId, template: TemplateBean): Future[Boolean] = {
    
    if (id.env == DefaultEnvironments.PROD.getName) {
      templateRepository.deployTemplate(id, template).map { x => x.wasApplied() }
    }
    else {
      templateRepository.updateTestingTemplate(id, id.env,
          template).map { x => x.wasApplied() }
    }
    
  }

  def find(id: TemplateId): Future[Option[TemplateInfo]] = {
    
    if (id.env == DefaultEnvironments.PROD.getName) {
      templateRepository.findDeployedTemplate(id).map(x => x.map(y => new TemplateInfo(y._2.getTime, y._1)))
    }
    else {
      templateRepository.findTestingTemplate(id, id.env).map(x => x.map(y => new TemplateInfo(0, y._1)))
    }
    
  }

  def delete(id: TemplateId, deployedAt: Long): Future[Boolean] = {
    
    if (id.env == DefaultEnvironments.PROD.getName) {
      templateRepository.rollbackTemplate(id, new Date(deployedAt)).map { x => x.wasApplied() }
    }
    else {
      templateRepository.deleteTestingTemplate(id, id.env).map { x => x.wasApplied() }
    }
    
  }

  
}

case class TemplateInfo(deployedAt: Long, template: Template)

case class TemplateId(accountId: String, domainId: String, templateId: String, env: String) extends TemplateRef

case class TemplateBean(

displayName: String, description: String, engine: TemplateEngineType, 
fromRecipients: String, bccRecipients: String,
subject: String, textBody: String, htmlBody: String

) extends Template


  