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
    println("  --port <int>           Zoe server port. (Optional, default 30000)")
    println
    println("  -d <string>")
    println("  --domain <string>      Zoe server domain.")
    println
    println("  -g <string>")
    println("  --gateway <string>     Server gateway domain.")    
    println
    println("  -c <path>")
    println("  --conf <path>          zoe.conf path")
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

      val confpath = opConfFile value match {
        case None => throw new Exception("Conf path needed")
        case Some(s) => s
      } 

      val conf = new Conf(new FileInputStream(confpath))
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