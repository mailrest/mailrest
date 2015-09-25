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
