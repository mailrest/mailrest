/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package modules

import scaldi._
import com.mailrest.maildal.repository.AccountRepository
import com.mailrest.maildal.config.MailDalConfig
import com.mailrest.maildal.config.RepositoryConfig
import com.mailrest.maildal.repository.AccountDomainRepository
import com.mailrest.maildal.repository.AccountLogRepository
import com.mailrest.maildal.repository.CookieRepository
import com.mailrest.maildal.repository.UserRepository
import com.mailrest.maildal.repository.UserLinkRepository
import com.mailrest.maildal.repository.DomainOwnerRepository
import com.mailrest.maildal.repository.DomainRepository
import com.mailrest.maildal.repository.MessageRepository
import com.mailrest.maildal.repository.TemplateRepository
import com.mailrest.maildal.repository.MessageQueueRepository
import com.mailrest.maildal.repository.MessageLogRepository
import com.mailrest.maildal.repository.MessageStatsDailyRepository
import com.mailrest.maildal.repository.UnsubscribedRecipientRepository

class DalModule extends Module {
  
  // Account
  
  bind [AccountRepository] to new RepositoryConfig(conf) with AccountRepository
  bind [AccountDomainRepository] to new RepositoryConfig(conf) with AccountDomainRepository
  bind [AccountLogRepository] to new RepositoryConfig(conf) with AccountLogRepository
  bind [CookieRepository] to new RepositoryConfig(conf) with CookieRepository  
  bind [UserRepository] to new RepositoryConfig(conf) with UserRepository  
  bind [UserLinkRepository] to new RepositoryConfig(conf) with UserLinkRepository  
  
  // Domain

  bind [DomainRepository] to new RepositoryConfig(conf) with DomainRepository
  bind [DomainOwnerRepository] to new RepositoryConfig(conf) with DomainOwnerRepository
  bind [TemplateRepository] to new RepositoryConfig(conf) with TemplateRepository

  // Message

  bind [MessageRepository] to new RepositoryConfig(conf) with MessageRepository
  bind [MessageLogRepository] to new RepositoryConfig(conf) with MessageLogRepository
  bind [MessageQueueRepository] to new RepositoryConfig(conf) with MessageQueueRepository
  bind [MessageStatsDailyRepository] to new RepositoryConfig(conf) with MessageStatsDailyRepository
  bind [UnsubscribedRecipientRepository] to new RepositoryConfig(conf) with UnsubscribedRecipientRepository

  // Config
  
  val conf = new MailDalConfig(inject [String] ('host), inject [String] ('keyspace))
  
  binding identifiedBy 'host to "cassandra.host"
  binding identifiedBy 'keyspace to "cassandra.keyspace"
  
}
