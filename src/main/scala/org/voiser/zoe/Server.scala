package org.voiser.zoe

class Server(val port: Int, val domain: String) { 

  def this(d: String) = this(30000, d)
  
  def local(mp: MessageParser) = mp get "dd" match {
    case None => true
    case Some(destination) => destination == domain
  }
  
  def loops(mp: MessageParser) = mp get "dd" match {
    case None => false
    case Some(destination) => mp.list("vd") match {
      case None => false
      case Some(visited) => visited.contains(destination)
    }
  }
}
