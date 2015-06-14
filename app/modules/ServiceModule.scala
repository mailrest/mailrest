/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package modules

import scala.concurrent.ExecutionContext
import scala.reflect.runtime.universe
import scaldi.Module
import services.AccountService
import services.AccountServiceImpl
import com.mailrest.maildal.secur.DefaultTokenManager
import com.mailrest.maildal.secur.AccountWebToken
import com.mailrest.maildal.secur.TokenManager
import services.DomainService
import services.DomainServiceImpl

class ServiceModule extends Module {
  
    bind [AccountService] to new AccountServiceImpl
    bind [DomainService] to new DomainServiceImpl
        
    bind [ExecutionContext] to ExecutionContext.global
}
