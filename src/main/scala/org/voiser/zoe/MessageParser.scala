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

class MessageParser(private val orig: String) {

  lazy val map = parse(orig)
  
  /**
   * Parses the input string and builds a key-value map
   */
  def parse(s : String) = {
    def split(pair: String) = {
      val splitPoint = pair.indexOf('=')
      val key = pair.substring(0, splitPoint)
      val value = pair.substring(splitPoint + 1)      
      (key, value)
    }
    def parse(pair : String, acc : Map[String, List[String]]) = {
      val (key, value) = split(pair)
      acc get key match {
        case None => acc + (key -> List(value)) 
        case Some(xs) => acc + (key -> (value :: xs))
      }
    }
    val pairs = s.split("&")    
    pairs.foldRight(Map[String, List[String]]())(parse)
  }
  
  /** 
   * Returns the first key of a key-value pair
   */
  def get(key: String) = map get key match {
    case None => None
    case Some(list) => Some(list.head)
  }
 
  /**
   * Returns a list of all values for a given key
   */
  def list(key: String) = map get key
  
  /**
   * 
   */
  override def toString() = orig
  
  /**
   * 
   */
  def bytes = orig getBytes
  
  /**
   * 
   */
  def log(header: String) {
    val cid = get("_cid") match {
      case None => "[no CID]"
      case Some(string) => string
    }
    println(cid + " " + header)
    for {
      key <- map.keySet
      value <- list(key)
    } println (cid + "   " + key + " = " + value.mkString(", "))
  }
}
