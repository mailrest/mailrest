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
import com.mailrest.maildal.repository.MessageRepository
import com.mailrest.maildal.repository.MessageLogRepository
import com.mailrest.maildal.repository.MessageQueueRepository
import com.mailrest.maildal.repository.MessageRepository.NewMessage
import java.util.Date
import com.mailrest.maildal.model.MessageType
import scala.concurrent.Future
import com.mailrest.maildal.model.Message
import utils.ScalaHelper
import com.mailrest.maildal.model.MessageRecipient

trait MessageService {

  def create(msg: NewMessageBean): Future[String]
  
  def find(msgId: String, domainId: DomainId): Future[Option[Message]]
  
  def delete(msgId: String, domainId: DomainId): Future[Boolean]
  
}

class MessageServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends MessageService with Injectable with LazyLogging {

  val messageRepository = inject[MessageRepository]
  val messageLogRepository = inject[MessageLogRepository]
  val messageQueueRepository = inject[MessageQueueRepository]

  def create(msg: NewMessageBean): Future[String] = {
    
      val recipients = collectRecipients(msg);
      
      messageRepository.createMessage(msg)
       .map(x => { x._2 })
       .flatMap{ msgId => 
         
         val fList = recipients.map(rec => enqueue(msgId, msg.deliveryAt, rec)) 
         
         val f = Future sequence fList
         
         f.map { x => x.head }
         
       }
    
  }
  
  def enqueue(msgId: String, deliveryAt: Date, rec: MessageRecipient): Future[String] = {
     messageQueueRepository.enqueueMessage(0, msgId, deliveryAt, 0, rec).map { x => msgId }
  }
  
  def collectRecipients(msg: NewMessageBean) : List[MessageRecipient] = {
    ScalaHelper.asList(msg.to) ::: ScalaHelper.asList(msg.cc) ::: ScalaHelper.asList(msg.bcc) 
  }
  
  def find(msgId: String, domainId: DomainId): Future[Option[Message]] = {
    
    messageRepository.findMessage(msgId, domainId)
    
  }
  
  def delete(msgId: String, domainId: DomainId): Future[Boolean] = {
    
    messageRepository.deleteMessage(msgId, domainId).map { x => x.wasApplied() }
    
  }
  
}

case class NewMessageBean(
  accountId: String, domainId: String,  
  messageType: MessageType,
  deliveryAt: Date, collisionId: String,    
  from: MessageRecipient, to: java.util.List[MessageRecipient], 
  cc: java.util.List[MessageRecipient], bcc: java.util.List[MessageRecipient],  
  subject: String, textBody: String, htmlBody: String
) extends NewMessage


