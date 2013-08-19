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
  
  class ConfException(msg: String) extends Exception(msg)

  def this(s: String) = this(new FileInputStream(s))
  
  val ini = new Ini(is)
  
  lazy val sections = JavaConversions.asScalaSet(ini.keySet())
  
  lazy val agents = sections filter { _ startsWith "agent " } map { _ substring 6 }

  lazy val topics = sections filter { _ startsWith "topic " } map { _ substring 6 }
  
  def section(name: String) = {
    val v = ini get name
    if (v != null) v
    else throw new ConfException("No section " + name)
  }
  
  def value(sec: String, name: String) = {
    val s = section(sec)
    val v = s.get(name)
    if (v != null) v
    else throw new ConfException("No value " + name + " in section " + sec)
  }
  
  def agentHost(agent: String) = {
    try value("agent " + agent, "host")
    catch { 
    case e:ConfException => "localhost"
    }
  }

  def agentPort(agent: String) = value ("agent " + agent, "port") toInt
  
  def agentsForTopic(topic: String) = value ("topic " + topic, "agents") split " "
  
  def domainHost(domain: String) = value ("domain " + domain, "host")

  def domainPort(domain: String) = { 
    try value("domain " + domain, "port") toInt
    catch {
    case e:ConfException => 30000
    }
  }
}
