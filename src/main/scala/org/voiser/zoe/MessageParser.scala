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
}
