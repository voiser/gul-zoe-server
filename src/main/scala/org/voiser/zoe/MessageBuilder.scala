package org.voiser.zoe

import java.util.UUID

class MessageBuilder(val map: Map[String, List[String]]) {
  
  /**
   * New builder with a random CID
   */
  def this() {
    this(Map("_cid" -> List(UUID.randomUUID().toString())))
  }

  /**
   * sets a key-value pair
   */
  def put(key: String, value: String) = new MessageBuilder(map + (key -> List(value)))
  
  /**
   * Adds a value to a key
   */
  def add(key: String, value: String) = map get key match {
    case None => new MessageBuilder(map + (key -> List(value)))
    case Some(list) => new MessageBuilder(map + (key -> (value :: list)))
  }
  
  /**
   * Creates a MB as a response of an original message.
   */
  def original(original: MessageParser) = original get "_cid" match {
    case None => this
    case Some(cid) => put("_cid", cid)
  }
  
  /**
   * Sets the destination agent
   */
  def dst(agent: String) = put("dst", agent)
  
  /**
   * Sets the source agent
   */
  def src(agent: String) = put("src", agent)
  
  /** 
   * Sets the destination domain
   */
  def dd(domain: String) = put("dd", domain)
  
  /**
   * Adds a domain to the visited domain list
   */
  def vd(domain: String) = add("vd", domain)

  /**
   * Returns the key-value collection, a key can appear multiple times
   */
  def pairs = for {
    (key, values) <- map.toSeq
    value <- values
  } yield (key, value)

  /**
   * Generates a string with all key-value pairs. 
   * key sep1 value sep2 key sep1 value sep2 ...
   */ 
  def mkString(sep1: String, sep2: String) = 
    pairs.map(p => p._1 + sep1 + p._2).mkString(sep2)
  
  /**
   * Generates the message as Zoe expects it
   */
  def msg = mkString("=", "&")
  
  /**
   * 
   */
  override def toString = mkString(" = ", " &\n")
}
