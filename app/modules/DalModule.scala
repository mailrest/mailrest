/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package modules

import scala.reflect.runtime.universe
import com.mailrest.maildal.config.MailDalConfig
import com.mailrest.maildal.config.RepositoryConfig
import com.mailrest.maildal.repository._
import scaldi.Injectable
import scaldi.Module
import scaldi.Injector
import play.api.Application
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config


class DalModule extends Module {
  
  // Account
  
  bind [AccountRepository] to new RepositoryConfig(dalConf) with AccountRepository
  bind [AccountDomainRepository] to new RepositoryConfig(dalConf) with AccountDomainRepository
  bind [AccountLogRepository] to new RepositoryConfig(dalConf) with AccountLogRepository
  bind [UserRepository] to new RepositoryConfig(dalConf) with UserRepository  
  
  // Domain

  bind [DomainRepository] to new RepositoryConfig(dalConf) with DomainRepository
  bind [DomainOwnerRepository] to new RepositoryConfig(dalConf) with DomainOwnerRepository
  bind [TemplateRepository] to new RepositoryConfig(dalConf) with TemplateRepository
  bind [DomainVerificationQueueRepository] to new RepositoryConfig(dalConf) with DomainVerificationQueueRepository
  
  // Message

  bind [MessageRepository] to new RepositoryConfig(dalConf) with MessageRepository
  bind [MessageLogRepository] to new RepositoryConfig(dalConf) with MessageLogRepository
  bind [MessageQueueRepository] to new RepositoryConfig(dalConf) with MessageQueueRepository
  bind [MessageStatsDailyRepository] to new RepositoryConfig(dalConf) with MessageStatsDailyRepository
  bind [UnsubscribedRecipientRepository] to new RepositoryConfig(dalConf) with UnsubscribedRecipientRepository

  // Config
  
  val appConf = ConfigFactory.load()
  
  //val cassandraHost = appConf.getString("cassandra.host")
  lazy val cassandraHost = inject [String]  ("cassandra.host")
  val cassandraKeyspace = appConf.getString("cassandra.keyspace")
  
  lazy val dalConf = new MailDalConfig(cassandraHost, cassandraKeyspace)
  
}
