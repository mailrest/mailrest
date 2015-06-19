package services

import com.typesafe.scalalogging.slf4j.LazyLogging
import scaldi.Injectable
import scala.concurrent.ExecutionContext
import scaldi.Injector
import com.mailrest.maildal.repository.MessageRepository
import com.mailrest.maildal.repository.MessageLogRepository
import com.mailrest.maildal.repository.MessageQueueRepository
import com.mailrest.maildal.repository.MessageRepository.NewMessage
import java.util.Date
import com.mailrest.maildal.model.MessageType
import scala.concurrent.Future
import com.mailrest.maildal.model.Message
import utils.ScalaHelper

trait MessageService {

  def create(msg: NewMessageBean): Future[String]
  
  def find(msgId: String, accountId: String, domainId: String): Future[Option[Message]]
  
  def delete(msgId: String, accountId: String, domainId: String): Future[Boolean]
  
}

class MessageServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends MessageService with Injectable with LazyLogging {

  val messageRepository = inject[MessageRepository]
  val messageLogRepository = inject[MessageLogRepository]
  val messageQueueRepository = inject[MessageQueueRepository]

  def create(msg: NewMessageBean): Future[String] = {
    
    messageRepository.createMessage(msg)
     .map(x => { x._2 })
     .flatMap(msgId => enqueue(msgId, msg.deliveryAt))
    
  }
  
  def enqueue(msgId: String, deliveryAt: Date): Future[String] = {
     messageQueueRepository.enqueueMessage(0, msgId, deliveryAt, 0).map { x => msgId }
  }
  
  def find(msgId: String, accountId: String, domainId: String): Future[Option[Message]] = {
    
    messageRepository.findMessage(msgId, accountId, domainId).map(ScalaHelper.asOption)
    
  }
  
  def delete(msgId: String, accountId: String, domainId: String): Future[Boolean] = {
    
    messageRepository.deleteMessage(msgId, accountId, domainId).map { x => x.wasApplied() }
    
  }
  
}

case class NewMessageBean(
  accountId: String, domainId: String,  
  messageType: MessageType,
  deliveryAt: Date, publicId: String,    
  from: String, to: String, cc: String, bcc: String,    
  templateId: String, userVariables: java.util.Map[String, String],    
  subject: String, textBody: String, htmlBody: String
) extends NewMessage


