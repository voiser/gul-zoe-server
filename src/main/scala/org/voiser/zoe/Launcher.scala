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

import org.clapper.argot._
import ArgotConverters._
import java.io.FileInputStream

object Launcher {
    
  val parser = new ArgotParser("Zoe server")
  val opPort = parser.option[Int](List("p", "port"), "n", "TCP port")
  val opDomain = parser.option[String](List("d", "domain"), "name", "Server domain")    
  val opGateway = parser.option[String](List("g", "gateway"), "name", "Gateway domain")
  val opConfFile = parser.option[String](List("c", "conf"), "name", "zoe.conf path")
  
  def usage {
    println("Parameters:")
    println
    println("  -p <int>")
    println("  --port <int>           Zoe server port. (optional, default 30000)")
    println
    println("  -d <string>")
    println("  --domain <string>      Zoe server domain (required)")
    println
    println("  -g <string>")
    println("  --gateway <string>     Server gateway domain (required)")    
    println
    println("  -c <path>")
    println("  --conf <path>          zoe.conf path (optional)")
  }
  
  def parse(args: Array[String]) {
      parser.parse(args)
      
      val port = opPort value match {
        case None => 30000
        case Some(p) => p
      }
      
      val domain = opDomain value match {
        case None => throw new Exception("Domain needed")
        case Some(s) => s
      }
      
      val gateway = opGateway value match {
        case None => throw new Exception("Gateway needed")
        case Some(s) => s
      } 

      val conf = new Conf()
      
      val confpath = opConfFile value match {
        case None => "(none)"
        case Some(s) => {
          ConfFileReader.register(new FileInputStream(s), conf)
          s
        }
      } 

      val server = new Server(port, domain, gateway, conf)
      
      println("Starting server on port " + port)
      println("  conf file: " + confpath)
      println("  domain: " + domain)
      println("  gateway: " + gateway)

      server start
  }
  
  def main(args: Array[String]): Unit = {
    try {
      parse(args)
    }
    catch {
      case e:Exception => usage
    }
  }
}