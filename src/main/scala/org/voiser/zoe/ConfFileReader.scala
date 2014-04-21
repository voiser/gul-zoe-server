/*
 * This file is part of Zoe Assistant - https://github.com/guluc3m/gul-zoe
 * 
 * Copyright (c) 2013 David Muñoz Díaz <david@gul.es> 
 * 
 * This file is distributed under the MIT LICENSE
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.voiser.zoe

import java.io.InputStream
import org.ini4j.Ini
import scala.collection.JavaConversions

/**
 * This object registers the contents of an old-fashioned zoe.conf
 */
object ConfFileReader {
  
  /**
   * 
   */
  def section(ini: Ini, name: String) = {
    val v = ini get name
    if (v != null) v
    else throw new Exception("No section '" + name + "'")
  }
  
  /**
   * 
   */
  def value(ini: Ini, sec: String, name: String) = {
    val s = section(ini, sec)
    val v = s.get(name)
    if (v != null) Some(v)
    else None
  }
  
  /**
   * 
   */
  def apply(is: InputStream) = {
    val ini = new Ini(is)
    val sections:List[String] = JavaConversions.asScalaSet(ini.keySet()).map(identity)(collection.breakOut)
    
    val agents = sections filter { _ startsWith "agent " } map { _ substring 6 }    
    val agentList = agents.map { name: String =>
      val host:String = value(ini, "agent " + name, "host") getOrElse "localhost"
      val port = value(ini, "agent " + name, "port") map {_.toInt } get;
      new Agent(name, host, port)
    }

    val topics = sections filter { _ startsWith "topic " } map { _ substring 6 }
    val topicList = topics.map { name: String =>
      val as = value(ini, "topic " + name, "agents") map { _.split(" ").toList } getOrElse List()
      new Topic(name, as)
    }

    val domains = sections filter { _ startsWith "domain " } map { _ substring 7 }
    val domainList = domains.map { name: String =>
      val h = value(ini, "domain " + name, "host") getOrElse "localhost"
      val p = value(ini, "domain " + name, "port") map { _.toInt } get;
      new Domain(name, h, p)
    }
    
    Conf(agentList, topicList, domainList)
  }
}
