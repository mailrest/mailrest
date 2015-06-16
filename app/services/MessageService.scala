package services

import com.typesafe.scalalogging.slf4j.LazyLogging
import scaldi.Injectable
import scala.concurrent.ExecutionContext
import scaldi.Injector
import com.mailrest.maildal.repository.MessageRepository
import com.mailrest.maildal.repository.MessageLogRepository
import com.mailrest.maildal.repository.MessageQueueRepository

trait MessageService {

}

class MessageServiceImpl(implicit inj: Injector, xc: ExecutionContext = ExecutionContext.global) extends MessageService with Injectable with LazyLogging {
 
      val messageRepository = inject [MessageRepository]
      val messageLogRepository = inject [MessageLogRepository]
      val messageQueueRepository = inject [MessageQueueRepository]

}