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

class Router (val domain: String, val gateway: String, val conf: Conf) { 
  
  /**
   * 
   */
  val identityTransformer = (mp: MessageParser) => mp
  
  /**
   * Transforms a message to be sent to the gateway
   */
  val gatewayTransformer = 
    (gateway: String, visited: String) => 
      (mp: MessageParser) => 
        new MessageParser(new MessageBuilder(mp map).dd(gateway).vd(visited).msg)
  
  /**
   * Builds a destination for a given agent
   */
  def ptpDest (agent: String) = 
    if (conf.agents.contains(agent)) Some(new Destination(conf agentHost(agent), conf agentPort(agent), identityTransformer))
    else None 
  
  /**
   * Builds a list of valid destinations for a given topic
   */
  def topicDest (topic: String) = 
    if (conf.topics.contains(topic)) conf agentsForTopic(topic) map { ptpDest(_) } filter { _.isDefined } map { _.get } toList
    else List()
    
  /**
   * Builds a list of destinations for local, point-to-point delivery
   */
  val ptpDispatcher = 
    (mp: MessageParser) => 
      mp get "dst" map {
        agent => ptpDest(agent) map { List(_) } getOrElse List()
      } getOrElse List()
  
  /**
   * Builds a list of destinations for local, topic delivery
   */
  val topicDispatcher = 
    (mp: MessageParser) => 
      mp get "topic" map {
        topic => topicDest(topic)
      } getOrElse List()
  
  /**
   * 
   */
  val dispatchers = List(ptpDispatcher, topicDispatcher)
  
  /**
   * <tt>true</tt> if the message must be locally delivered
   */
  def local(mp: MessageParser) = mp get "dd" map { _ == domain } getOrElse true
  
  /**
   * <tt>true</tt> if the message will be delivered to an already visited domain
   */
  def loops(mp: MessageParser) = mp get "dd" match {
    case None => false
    case Some(destination) => mp.list("vd") match {
      case None => false
      case Some(visited) => visited.contains(destination)
    }
  }
  
  /**
   * Bulds a list of destinations for local delivery
   */
  def localDestinations(mp: MessageParser) = {
    val destinations = for (d <- dispatchers) yield d(mp)
    destinations.flatten.distinct
  }
  
  /**
   * Bulds a list of destinations for remote delivery
   */
  def remoteDestinations(mp: MessageParser): List[Destination] = 
    mp get "dd" map { 
      x => List(new Destination(conf domainHost(x), conf domainPort(x), identityTransformer))
    } getOrElse List()
  
  /**
   * Calculates the destinations an incoming message should be sent to.
   * Every destination contains a host, a port and a transformer, which is a function
   * that should be applied to the incoming message before sending it to the destination host/port
   */
  def destinations(mp: MessageParser) = {
    if (local(mp)) {
      localDestinations(mp) match {
        case List() => List(new Destination(conf domainHost(gateway), conf domainPort(gateway), gatewayTransformer(gateway, domain)))
        case x => x 
      }
    } 
    else {
      if (!loops(mp)) remoteDestinations(mp)
      else List()
    }
  }
}
