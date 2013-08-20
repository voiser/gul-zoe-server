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
