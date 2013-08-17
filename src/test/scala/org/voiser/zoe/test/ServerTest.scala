package org.voiser.zoe.test

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.voiser.zoe.MessageParser
import org.voiser.zoe.MessageBuilder
import org.voiser.zoe.Server
import org.voiser.zoe.MessageParser

@RunWith(classOf[JUnitRunner])
class ServerTest extends FunSuite {

  trait Fixtures {
    val s1 = new Server(1, "domain1")
  }

  test("No dd means local delivery") {
    new Fixtures {
      val m = new MessageParser("dst=mydest")
      assert(s1 local m)
    }
  }

  test("dd == my domain means local delivery") {
    new Fixtures {
      val m = new MessageParser("dst=mydest&dd=domain1")
      assert(s1 local m)
    }
  }

  test("dd != my domain means non-local delivery") {
    new Fixtures {
      val m = new MessageParser("dst=mydest&dd=domain2")
      assert(!(s1 local m))
    }
  }

  test("detect loops") {
    new Fixtures {
      val m = new MessageParser("vd=A&vd=B&dd=A&dst=agent")
      assert(s1 loops m)
    }
  }

  test("detect loops 2") {
    new Fixtures {
      val m = new MessageParser("vd=A&vd=B&dd=C&dst=agent")
      assert(!(s1 loops m))
    }
  }
}


