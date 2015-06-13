/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package utils

import java.util.Optional
import java.util.Collection



object ScalaHelper {

  def asOption[X](input: Optional[X]): Option[X] = {
    
    if (input.isPresent()) {
      Some(input.get)
    }
    else {
      None
    }
    
  }
  
  def asSeq[X](col: Collection[X]): Seq[X] = {
    
    scala.collection.JavaConversions.asScalaIterator(col.iterator()).toSeq
    
  }
  
}