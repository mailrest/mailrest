/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package modules

import scaldi._
import services.AccountService
import services.AccountServiceImpl
import scala.concurrent.ExecutionContext

class ServiceModule extends Module {

    bind [AccountService] to new AccountServiceImpl

    bind [ExecutionContext] to ExecutionContext.global
}
