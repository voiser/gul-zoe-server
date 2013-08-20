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

import scala.actors.Actor
import scala.actors.Actor._
import java.net.Socket

class Dispatcher(router: Router) extends Actor {

  /**
   * The actor loop 
   */
  def act() = {
    loop {
      react {
        case mp: MessageParser => {
          for (dest <- router.destinations(mp)) {
            println("Dispatching message " + mp + " to destination " + dest)
            this ! new Message(mp, dest)
          }
        }
        case Message(mp, dest) => {
            val host = dest.host
            val port = dest.port
            val tr = dest.transformer
            val msg = tr(mp)
            println("  message transformed to " + msg)
            send(msg, host, port)          
        }
      }
    }
  }

  /**
   * Avoid exceptions to stop the actor.
   */
  override def exceptionHandler = {
    case e: Exception => e.printStackTrace()
  }
  
  /**
   * Sends a message to a host:port
   */
  def send(mp: MessageParser, host: String, port: Int) = {
    println("  sending " + mp + " to " + host + ":" + port)
    val socket = new Socket(host, port)
    val os = socket.getOutputStream()
    os.write(mp.bytes)
    os.close
    println("  sent")
  }
}
