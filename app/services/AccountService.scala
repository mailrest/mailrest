/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package services

import com.mailrest.maildal.repository.AbstractRepository
import com.noorq.casser.core.CasserSession
import com.noorq.casser.core.Casser
import scaldi.Injectable
import scaldi.Injector
import com.mailrest.maildal.repository.AccountRepository

trait AccountService {

  
}

class AccountServiceImpl(implicit inj: Injector) extends AccountService with Injectable {
 
  val accountRepository = inject [AccountRepository]
  
}