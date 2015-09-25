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
package controllers.account

import controllers.action.AccountAction
import controllers.action.AccountAdminAction
import controllers.action.AccountReadAction
import controllers.action.AccountWriteAction
import scaldi.Injectable
import scaldi.Injector
import play.api.mvc.Controller

abstract class AbstractAccountController(implicit inj: Injector) extends Controller with Injectable {

    val accountAction = inject [AccountAction]
  
    def readAction(accId: String) = accountAction andThen new AccountReadAction(accId)
    
    def writeAction(accId: String) = accountAction andThen new AccountWriteAction(accId)
    
    def adminAction(accId: String) = accountAction andThen new AccountAdminAction(accId)
    
}