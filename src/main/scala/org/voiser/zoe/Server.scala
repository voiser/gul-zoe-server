package org.voiser.zoe

class Server(val port: Int, val domain: String, val conf: Conf) { 

  class Destination(val host: String, val port: Int) {
    override def toString() = host + ":" + port
    override def hashCode() = 41 * (host.hashCode() + 41) + port
    override def equals(other: Any) = other match {
      case o: Destination => o.host == this.host && o.port == this.port && o.getClass == this.getClass
      case _ => false
    }
  }
  
  val ptpDispatcher = (mp: MessageParser) => mp get "dst" match {
    case None => List()
    case Some(agent) => List(new Destination(conf agentHost(agent), conf agentPort(agent)))
  }
  
  val topicDispatcher = (mp: MessageParser) => mp get "topic" match {
    case None => List()
    case Some(topic) => {
      val dests = for {agent <- conf agentsForTopic(topic)} yield new Destination(conf agentHost(agent), conf agentPort(agent))
      dests toList
    }
  }
  
  val dispatchers = List(ptpDispatcher, topicDispatcher)
  
  def this(d: String, conf: Conf) = this(30000, d, conf)
  
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

  def localDestinations(mp: MessageParser) = {
    val destinations = for (d <- dispatchers) yield d(mp)
    destinations.flatten.distinct
  }
  
  def remoteDestinations(mp: MessageParser): List[Destination] = {
    mp get "dd" match {
      case None => List()
      case Some(domain) => List(new Destination(conf domainHost(domain), conf domainPort(domain)))
    }
  }
  
  def destinations(mp: MessageParser) = {
    if (local(mp)) localDestinations(mp)
    else {
      if (!loops(mp)) remoteDestinations(mp)
      else List()
    }
  }
}
