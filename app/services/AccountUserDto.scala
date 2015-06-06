/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package services

import com.mailrest.maildal.model.AccountUser

case class AccountUserDto(email: String, firstName: String, lastName: String) extends AccountUser {
  
}
