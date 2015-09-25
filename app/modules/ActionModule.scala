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

