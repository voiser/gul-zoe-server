package org.voiser.zoe.test

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.voiser.zoe.MessageParser
import org.voiser.zoe.MessageBuilder
import org.voiser.zoe.Conf
import org.voiser.zoe.Agent
import org.voiser.zoe.Topic
import org.voiser.zoe.Domain

@RunWith(classOf[JUnitRunner])
class ConfTest extends FunSuite {
  
  test("Empty conf") {
    val conf = Conf()
    assert(conf.agents === Map[String, Agent]())
    assert(conf.topics === Map[String, Topic]())
    assert(conf.domains === Map[String, Domain]());
  }
  
  test("Agent registration") {
    val conf = Conf()
    val agent = new Agent("agent", "localhost", 10000);
    val conf2 = conf.register(agent)
    assert(conf2.agents === Map[String, Agent]() + (agent.name -> agent))
    assert(conf2.topics === Map[String, Topic]())
    assert(conf2.domains === Map[String, Domain]());
  }

  test("Agent registration (twice)") {
    val conf = Conf()
    val agent = new Agent("agent", "localhost", 10000);
    val conf2 = conf.register(agent).register(agent)
    assert(conf2.agents === Map[String, Agent]() + (agent.name -> agent))
    assert(conf2.topics === Map[String, Topic]())
    assert(conf2.domains === Map[String, Domain]());
  }
  
  test("Topic registration") {
    val conf = Conf()
    val topic = new Topic("topic", List())
    val conf2 = conf.register(topic)
    assert(conf2.agents === Map[String, Agent]())
    assert(conf2.topics === Map[String, Topic]() + (topic.name -> topic))
    assert(conf2.domains === Map[String, Domain]());
  }
  
  test("Topic registration (twice)") {
    val conf = Conf()
    val topic = new Topic("topic", List())
    val conf2 = conf.register(topic).register(topic)
    assert(conf2.agents === Map[String, Agent]())
    assert(conf2.topics === Map[String, Topic]() + (topic.name -> topic))
    assert(conf2.domains === Map[String, Domain]());
  }

  test("Agent & topic registration") {
    val conf = Conf()
    val agent = new Agent("agent", "localhost", 10000);
    val topic = new Topic("topic", List())
    val conf2 = conf.register(agent, topic)
    assert(conf2.agents === Map[String, Agent]() + (agent.name -> agent))
    assert(conf2.domains === Map[String, Domain]());
    assert(conf2.topics.size === 1)
    val t = conf2.topics(topic.name)
    assert(t.name === topic.name)
    assert(t.agents === List(agent.name))
  }

  test("Agent & topic registration 2") {
    val conf = Conf()
    val agent = new Agent("agent", "localhost", 10000);
    val agent2 = new Agent("agent2", "localhost", 10001);
    val topic = new Topic("topic", List())
    val conf2 = conf.register(agent, topic).register(agent2, topic)
    val agents = conf2.agentsAt(topic.name)
    assert(conf2.topics.size == 1)
    val t = conf2.topics(topic.name)
    assert(t.name === topic.name)
    assert(t.agents === List(agent2.name, agent.name))
  }

  test("Agent & topic registration 3") {
    val conf = Conf()
    val agent = new Agent("agent", "localhost", 10000);
    val agent2 = new Agent("agent2", "localhost", 10001);
    val topic = new Topic("topic", List(agent2.name))
    val conf2 = conf.register(agent).register(agent2).register(agent, topic)
    val agents = conf2.agentsAt(topic.name)
    assert(conf2.topics.size == 1)
    val t = conf2.topics(topic.name)
    assert(t.name === topic.name)
    assert(t.agents === List(agent.name, agent2.name))    
  }
}


