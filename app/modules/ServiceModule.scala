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

class ServiceModule extends Module {
  
    bind [AccountService] to new AccountServiceImpl

    bind [ExecutionContext] to ExecutionContext.global
}
