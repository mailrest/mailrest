/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package modules

import scala.concurrent.ExecutionContext
import scala.reflect.runtime.universe
import scaldi.Module
import controllers.action.DomainAction
import controllers.action.AccountAction
import com.mailrest.maildal.secur.DefaultTokenManager
import com.mailrest.maildal.secur.AccountWebToken
import com.mailrest.maildal.secur.TokenManager
import com.mailrest.maildal.secur.UnsubscribeWebToken
import com.mailrest.maildal.secur.CallbackWebToken

class ActionModule extends Module {

  bind [AccountAction] to new AccountAction
  
  bind [TokenManager[AccountWebToken]] to new DefaultTokenManager[AccountWebToken](new AccountWebToken())
  bind [TokenManager[CallbackWebToken]] to new DefaultTokenManager[CallbackWebToken](new CallbackWebToken())
  bind [TokenManager[UnsubscribeWebToken]] to new DefaultTokenManager[UnsubscribeWebToken](new UnsubscribeWebToken())
      
}

