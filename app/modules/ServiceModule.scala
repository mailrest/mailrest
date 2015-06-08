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

class ServiceModule extends Module {
  
    bind [AccountService] to new AccountServiceImpl

    bind [ExecutionContext] to ExecutionContext.global
}
