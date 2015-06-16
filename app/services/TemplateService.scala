package services

import com.typesafe.scalalogging.slf4j.LazyLogging
import scaldi.Injectable
import scala.concurrent.ExecutionContext
import scaldi.Injector
import com.mailrest.maildal.repository.TemplateRepository
import com.mailrest.maildal.model.Template
import com.mailrest.maildal.model.TemplateEngine
import scala.concurrent.Future
import utils.ScalaHelper
import com.mailrest.maildal.model.DefaultEnvironments
import java.util.Optional
import com.noorq.casser.support.Fun
import java.util.Date

trait TemplateService {

  def update(id: TemplateId, template: TemplateBean): Future[Boolean]
  
  def find(id: TemplateId): Future[Option[TemplateInfo]]
  
  def delete(id: TemplateId, deployedAt: Long): Future[Boolean]
  
}

class TemplateServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends TemplateService with Injectable with LazyLogging {
 
  val templateRepository = inject [TemplateRepository]
  
  def update(id: TemplateId, template: TemplateBean): Future[Boolean] = {
    
    if (id.env == DefaultEnvironments.PROD.getName) {
      templateRepository.deployTemplate(id.domainId, id.accountId, id.templateId, template).map { x => x.wasApplied() }
    }
    else {
      templateRepository.updateTestingTemplate(id.domainId, id.accountId, id.templateId, id.env,
          template).map { x => x.wasApplied() }
    }
    
  }

  def find(id: TemplateId): Future[Option[TemplateInfo]] = {
    
    if (id.env == DefaultEnvironments.PROD.getName) {
      templateRepository.findDeployedTemplate(id.domainId, id.accountId, id.templateId).map { x => {
        
      if (x.isPresent()) {
        Some(new TemplateInfo(x.get._2.getTime, x.get._1))
      }
      else {
        None
      }
        
      } }
        
    }
    else {
      templateRepository.findTestingTemplate(id.domainId, id.accountId, id.templateId, id.env).map { x => {
        
      if (x.isPresent()) {
        Some(new TemplateInfo(0, x.get._1))
      }
      else {
        None
      }
        
      } }
    }
    
  }

  def delete(id: TemplateId, deployedAt: Long): Future[Boolean] = {
    
    if (id.env == DefaultEnvironments.PROD.getName) {
      templateRepository.rollbackTemplate(id.domainId, id.accountId, id.templateId, new Date(deployedAt)).map { x => x.wasApplied() }
    }
    else {
      templateRepository.deleteTestingTemplate(id.domainId, id.accountId, id.templateId, id.env).map { x => x.wasApplied() }
    }
    
  }

  
}

case class TemplateInfo(deployedAt: Long, template: Template)

case class TemplateId(domainId: String, accountId: String, templateId: String, env: String)

case class TemplateBean(

name: String, description: String, engine: TemplateEngine, 
fromRecipients: String, bccRecipients: String,
subject: String, textBody: String, htmlBody: String

) extends Template


  