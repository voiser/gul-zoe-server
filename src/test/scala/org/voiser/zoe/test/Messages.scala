package org.voiser.zoe.test

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.voiser.zoe.MessageParser
import org.voiser.zoe.MessageBuilder

@RunWith(classOf[JUnitRunner])
class Messages extends FunSuite {

  trait TestMessages {
    val m1 = "a=a1&a=a2&b=b1&c==c1="
    val mp1 = new MessageParser(m1)
    val mb = new MessageBuilder().
        add("a", "a1").
        add("a", "a2").
        add("b", "b1").
        add("c", "=c1=")
  }
  
  test("Simple parsing") {
    new TestMessages {
      assert(mp1.list("a") === Some(List("a1", "a2")))
      assert(mp1.get("b") === Some("b1"))
      assert(mp1.get("c") === Some("=c1="))
    }
  }
  
  test("Simple building") {
    new TestMessages {
      val m2 = mb.msg
      val mp2 = new MessageParser(m2)
      assert(mp2.list("a") === Some(List("a2", "a1")))
      assert(mp2.get("b") === Some("b1"))
      assert(mp2.get("c") === Some("=c1="))
    }
  }
}


