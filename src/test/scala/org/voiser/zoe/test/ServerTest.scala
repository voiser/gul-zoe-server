package org.voiser.zoe.test

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.voiser.zoe.MessageParser
import org.voiser.zoe.MessageBuilder
import org.voiser.zoe.Server
import org.voiser.zoe.MessageParser
import org.voiser.zoe.Conf
import org.voiser.zoe.Router
import java.net.Socket
import java.net.ServerSocket
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class ServerTest extends FunSuite {

  trait Fixtures {
    val is = this.getClass().getResourceAsStream("/zoe.conf")
    val conf = new Conf(is)
    val s1 = new Server(30000, "domain1", "gateway", conf)
    val r1 = s1.router
  }

  test("No dd means local delivery") {
    new Fixtures {
      val m = new MessageParser("dst=mydest")
      assert(r1 local m)
    }
  }

  test("dd == my domain means local delivery") {
    new Fixtures {
      val m = new MessageParser("dst=mydest&dd=domain1")
      assert(r1 local m)
    }
  }

  test("dd != my domain means non-local delivery") {
    new Fixtures {
      val m = new MessageParser("dst=mydest&dd=domain2")
      assert(!(r1 local m))
    }
  }

  test("detect loops") {
    new Fixtures {
      val m = new MessageParser("vd=A&vd=B&dd=A&dst=agent")
      assert(r1 loops m)
    }
  }

  test("detect loops 2") {
    new Fixtures {
      val m = new MessageParser("vd=A&vd=B&dd=C&dst=agent")
      assert(!(r1 loops m))
    }
  }
  
  test("local delivery, known agent, no dd") {
    new Fixtures {
      val m = new MessageParser("dst=agent1")
      val dest = r1.destinations(m)
      assert(dest.length === 1)
      assert(dest(0).host === "localhost")
    }
  }

  test("local delivery, known agent, dd") {
    new Fixtures {
      val m = new MessageParser("dst=agent1&dd=domain1")
      val dest = r1.destinations(m)
      assert(dest.length === 1)
      assert(dest(0).host === "localhost")
    }
  }

  test("remote delivery") {
    new Fixtures {
      val m = new MessageParser("dst=agent1&dd=extern1")
      val dest = r1.destinations(m)
      assert(dest.length === 1)
      assert(dest(0).host === "192.168.1.100")
    }
  }
  
  test("fallback to gateway") {
    new Fixtures {
      val m = new MessageParser("dst=agentX")
      val dest = r1.destinations(m)
      assert(dest.length === 1)
      assert(dest(0).host === "localhost")
      assert(dest(0).port === 31000)
    }
  }

  test("fallback to gateway with dd") {
    new Fixtures {
      val m = new MessageParser("dst=agentX&dd=domain1")
      val dest = r1.destinations(m)
      assert(dest.length === 1)
      assert(dest(0).host === "localhost")
      assert(dest(0).port === 31000)
    }
  }

  class Listener(port: Int) extends Runnable {
    var contents: String = ""
    def run() = {
      val socket = new ServerSocket(port)
      socket.setReuseAddress(true)
      val s = socket.accept()
      contents = Source.fromInputStream(s.getInputStream(), "UTF-8").mkString("")
      socket.close
    }
  }
  
  test("dispatch point-to-point") {
    new Fixtures {
      val l1 = new Listener(30100)
      val t1 = new Thread(l1)
      t1.start()
      val m = new MessageParser("dst=agent1")
      s1.dispatch(m)
      t1.join()
      assert(m.toString === l1.contents)
    }
  }

  test("dispatch topic") {
    new Fixtures {
      val l1 = new Listener(30100)
      val t1 = new Thread(l1)
      t1.start()
      val l2 = new Listener(30200)
      val t2 = new Thread(l2)
      t2.start()
      val m = new MessageParser("topic=topic1")
      s1.dispatch(m)
      t1.join()
      t2.join()
      assert(m.toString === l1.contents)
      assert(m.toString === l2.contents)
    }
  }
  
  test("dispatch unknown") {
    new Fixtures {
      val l1 = new Listener(31000)
      val t1 = new Thread(l1)
      t1.start()
      val m = new MessageParser("dst=unknown&payload=ABC")
      s1.dispatch(m)
      t1.join()
      val mp = new MessageParser(l1.contents)
      assert(mp.get("dst") === Some("unknown"))
      assert(mp.get("dd") === Some("gateway"))
      assert(mp.list("vd") === Some(List("domain1")))
      assert(mp.get("payload") === Some("ABC"))
    }
  }
}

