/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package utils

import java.util.Optional

object ScalaHelper {

  def optional[X](input: Optional[X]): Option[X] = {
    
    if (input.isPresent()) {
      Some(input.get)
    }
    else {
      None
    }
    
  }
  
}