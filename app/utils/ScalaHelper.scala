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
  
  def toSeq[X](stream: java.util.stream.Stream[X]): Seq[X] = {
    
    scala.collection.JavaConversions.asScalaIterator(stream.iterator()).toSeq
    
  }
  
  def toStream[X](stream: java.util.stream.Stream[X]): Stream[X] = {
    
    scala.collection.JavaConversions.asScalaIterator(stream.iterator()).toStream
    
  }
  
}