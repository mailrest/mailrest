/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package modules

import scaldi._
import services.AccountService
import services.AccountServiceImpl

class ServiceModule extends Module {

    bind [AccountService] to new AccountServiceImpl
    
}
