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

import org.ini4j.Wini
import java.io.File
import org.ini4j.Ini
import scala.collection.JavaConversions
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import org.ini4j.Profile.Section

class Conf(val is: InputStream) {
  
  /**
   * 
   */
  class ConfException(msg: String) extends Exception(msg)

  /**
   * 
   */
  def this(s: String) = this(new FileInputStream(s))
  
  /**
   * 
   */
  val ini = new Ini(is)
  
  /**
   * 
   */
  lazy val sections = JavaConversions.asScalaSet(ini.keySet())
  
  /**
   * 
   */
  lazy val agents = sections filter { _ startsWith "agent " } map { _ substring 6 }

  /**
   * 
   */
  lazy val topics = sections filter { _ startsWith "topic " } map { _ substring 6 }
  
  /**
   * 
   */
  def section(name: String) = {
    val v = ini get name
    if (v != null) v
    else throw new ConfException("No section " + name)
  }
  
  /**
   * 
   */
  def value(sec: String, name: String) = {
    val s = section(sec)
    val v = s.get(name)
    if (v != null) v
    else throw new ConfException("No value " + name + " in section " + sec)
  }
  
  /**
   * 
   */
  def agentHost(agent: String) = {
    try value("agent " + agent, "host")
    catch { 
    case e:ConfException => "localhost"
    }
  }

  /**
   * 
   */
  def agentPort(agent: String) = value ("agent " + agent, "port") toInt
  
  /**
   * 
   */
  def agentsForTopic(topic: String) = value ("topic " + topic, "agents") split " "

  /**
   * 
   */  
  def domainHost(domain: String) = value ("domain " + domain, "host")

  /**
   * 
   */
  def domainPort(domain: String) = { 
    try value("domain " + domain, "port") toInt
    catch {
    case e:ConfException => 30000
    }
  }
}
