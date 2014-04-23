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

import java.net.Socket
import akka.actor.Actor

class Dispatcher(r: Router) extends Actor {
  
  /**
   * The agent receiving function
   */
  def receive = dispatch(r)
  
  /**
   * Dispatches an incoming message
   */
  def dispatch(router:Router):Receive = {

    case mp: MessageParser => 
      println("Received MP: " + mp)
      val messages = analyze(router, mp)
      messages.foreach { m => self ! m }
    
    case AgentMessage(mp, dest) => 
      println("Received AM: " + mp + " -> " + dest)
      send(mp, dest)
    
    case ServerMessage(mp) => 
      println("Received SM: " + mp)
      mp.get("tag") match {
        
        case Some("register") =>
          val name = mp.get("name") get
          val host = mp.get("host") get
          val port = mp.get("port") map { _.toInt } get
          val agent = new Agent(name, host, port)
          val topics = mp.list("topic") getOrElse(List()) map { name => new Topic(name, List()) } 
          val newConf1 = router.conf.register(agent)
          val newConf2 = topics.foldLeft(newConf1) { (conf, topic) => conf.register(agent, topic) }
          val newRouter = router.withConf(newConf2)
          println("Registering " + agent)
          newConf2.log
          context.become(dispatch(newRouter), true)
          
        case _ => 
          println("A message was sent to the server with an invalid tag. Ignoring it.")
      }
  }
  
  /**
   * Generates a bunch of messages from a MessageParser
   */
  def analyze(router: Router, mp: MessageParser) =
    mp.get("dst") match {
      case Some("server") => List(ServerMessage(mp))
      case _ => router.destinations(mp) map { dest: Destination => new AgentMessage(mp, dest) }
    }
    
  /**
   * Sends a message to a destination
   */
  def send(mp: MessageParser, dest: Destination) {
    val host = dest.host
    val port = dest.port
    val msg = dest.transformer(mp)
    send(msg, host, port)
  }
  
  /**
   * Sends a message to a host:port
   */
  def send(mp: MessageParser, host: String, port: Int) = {
    val socket = new Socket(host, port)
    val os = socket.getOutputStream()
    os.write(mp.bytes)
    os.close
  }
}
