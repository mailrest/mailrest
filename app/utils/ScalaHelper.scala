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

  def asList[X](col: Collection[X]): List[X] = {

    scala.collection.JavaConversions.asScalaIterator(col.iterator()).toList
    
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