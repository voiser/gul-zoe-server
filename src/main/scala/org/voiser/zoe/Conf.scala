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

import java.util.NoSuchElementException

class Agent (val name: String, val host: String, val port: Int) extends Host {
  override def toString() = "agent-" + name + "@" + host + ":" + port
}
  
class Domain (val name: String, val host: String, val port: Int) extends Host {
  override def toString() = "domain-" + name + "@" + host + ":" + port
}
  
class Topic (val name: String, val agents: List[String]) {
  override def toString() = "topic-" + name + ":" + agents.mkString(",")
}
  
class Conf (val agents: Map[String, Agent],
            val topics: Map[String, Topic], 
            val domains: Map[String, Domain]) {
  
  def register(a: Agent) = new Conf(agents + (a.name -> a), topics, domains)
  
  def register(t: Topic) = new Conf(agents, topics + (t.name -> t), domains)
  
  def register(a: Agent, t: Topic):Conf = {
    val topicAgents = 
      if (topics.contains(t.name)) a.name :: t.agents ::: topics(t.name).agents
      else a.name :: t.agents      
    val topic = new Topic(t.name, topicAgents.distinct)
    new Conf(
        agents + (a.name -> a),
        topics + (t.name -> topic), 
        domains)
  }
  
  def agentsAt(topic: String) = {
    val agents0 = 
      if (topics.contains(topic)) topics(topic).agents
      else List[String]()
    agents0 map { a: String => agents(a) }
  }
} 

object Conf {
  
  def apply() = new Conf(Map[String, Agent](), Map[String, Topic](), Map[String, Domain]());
  
  def apply(agents: List[Agent], 
            topics: List[Topic], 
            domains: List[Domain]) = {
    
    val agentMap = agents.map { a: Agent => (a.name, a) } toMap
    val topicMap = topics.map { t: Topic => (t.name, t) } toMap
    val domainMap = domains.map { d: Domain => (d.name, d) } toMap
    
    new Conf(agentMap, topicMap, domainMap)
  }
}