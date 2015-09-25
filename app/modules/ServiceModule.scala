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
import services.AccountService
import services.AccountServiceImpl
import com.mailrest.maildal.secur.DefaultTokenManager
import com.mailrest.maildal.secur.AccountWebToken
import com.mailrest.maildal.secur.TokenManager
import services.DomainService
import services.DomainServiceImpl
import services.MessageService
import services.TemplateServiceImpl
import services.TemplateService
import services.MessageServiceImpl

class ServiceModule extends Module {
  
    bind [AccountService] to new AccountServiceImpl
    bind [DomainService] to new DomainServiceImpl
    bind [TemplateService] to new TemplateServiceImpl
    bind [MessageService] to new MessageServiceImpl
        
    bind [ExecutionContext] to ExecutionContext.global
}
