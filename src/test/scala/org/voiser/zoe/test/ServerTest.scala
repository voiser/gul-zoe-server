package org.voiser.zoe.test

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.voiser.zoe.MessageParser
import org.voiser.zoe.MessageBuilder
import org.voiser.zoe.Server
import org.voiser.zoe.MessageParser
import org.voiser.zoe.Conf

@RunWith(classOf[JUnitRunner])
class ServerTest extends FunSuite {

  trait Fixtures {
    val is = this.getClass().getResourceAsStream("/zoe.conf")
    val conf = new Conf(is)
    val s1 = new Server(1, "domain1", "gateway", conf)
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
  
  test("local delivery, known agent, no dd") {
    new Fixtures {
      val m = new MessageParser("dst=agent1")
      val dest = s1.destinations(m)
      assert(dest.length === 1)
      assert(dest(0).host === "localhost")
    }
  }

  test("local delivery, known agent, dd") {
    new Fixtures {
      val m = new MessageParser("dst=agent1&dd=domain1")
      val dest = s1.destinations(m)
      assert(dest.length === 1)
      assert(dest(0).host === "localhost")
    }
  }

  test("remote delivery") {
    new Fixtures {
      val m = new MessageParser("dst=agent1&dd=extern1")
      val dest = s1.destinations(m)
      assert(dest.length === 1)
      assert(dest(0).host === "192.168.1.100")
    }
  }
  
  test("fallback to gateway") {
    new Fixtures {
      val m = new MessageParser("dst=agentX")
      val dest = s1.destinations(m)
      assert(dest.length === 1)
      assert(dest(0).host === "192.168.100.100")
    }
  }

  test("fallback to gateway with dd") {
    new Fixtures {
      val m = new MessageParser("dst=agentX&dd=domain1")
      val dest = s1.destinations(m)
      assert(dest.length === 1)
      assert(dest(0).host === "192.168.100.100")
    }
  }
}


