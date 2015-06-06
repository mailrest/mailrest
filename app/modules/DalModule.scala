/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package modules

import scaldi._
import com.mailrest.maildal.repository.AccountRepository
import com.mailrest.maildal.config.MailDalConfig
import com.mailrest.maildal.config.RepositoryConfig

class DalModule extends Module {
  
  bind [AccountRepository] to new RepositoryConfig(conf) with AccountRepository
  
  val conf = new MailDalConfig(inject [String] ('host))
  
  binding identifiedBy 'host to "cassandra.host"
  
}
