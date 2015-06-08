/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package modules

import scala.concurrent.ExecutionContext
import scala.reflect.runtime.universe
import scaldi.Module
import controllers.action.DomainAction

class ActionModule extends Module {

  bind [DomainAction] to new DomainAction
  
}