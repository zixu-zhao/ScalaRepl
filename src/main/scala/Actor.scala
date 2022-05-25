import akka.actor.{Actor, ActorSystem, Props}

import scala.language.postfixOps

/**
 * Copyright nDimensional, Inc. 2015. All rights reserved.
 */
object Actor extends App {
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // part2 create actors
  // * Actors are uniquely identified
  // * messages are asynchronous
  // * each actor may respond differently
  // * actors are (really) encapsulated

  // word count actor

  class WordCountActor extends Actor {
    var totalWords = 0

    // PartialFunction[Any, Unit] = Receive
    override def receive: Receive = {
      case message: String =>
        println(s"[word counter] I have received $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }

  // part3 instantiate actor

  val wordCounter1 = actorSystem.actorOf(Props[WordCountActor], "wordCounter1")

  // part4 communicate with actor

  wordCounter1 ! "sending message 1"
  // asynchronous

  val wordCounter2 = actorSystem.actorOf(Props[WordCountActor], "wordCounter2")
  wordCounter2 ! "sending message 2"

  wordCounter1 tell("1 to 2", wordCounter2)

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"hi, my name is $name")
      case _ =>
    }
  }

  object Person {
    def props(name: String) = Props(new Person(name))
  }

  val personActor = actorSystem.actorOf(Person.props("zixu"), "PersonActor1")

  personActor ! "hi"


  case class SpecialContent(content: String)

  val Carol = actorSystem.actorOf(Props[SimpleActor], "carolActor")

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message: String => println(s"[simple actor] ${self.path} have received $message")
      case number: Int => println(s"[simple actor] I have received number $number")
      case sc: SpecialContent => println(s"[simple actor] I have received SpecialContent ${sc.content}")
      case sf: SendMessageToYourSelf => self ! sf.content
      case fm: ForwardMessage => {
        Carol forward s"from ${self.path} to carol, hello!"
        sender() ! s"from ${self.path} to sender, hello!"
      }
      case _ => println(context.self)
    }
  }

  val simpleActor1 = actorSystem.actorOf(Props[SimpleActor], "SimpleActor1")

  simpleActor1 ! "hello actor!"

  // 1 - messages can be of any type
  // * message must be immutable
  // * message must be serializable
  simpleActor1 ! 42 // who is the sender? (No sender and Response will be in dead letters)

  simpleActor1 ! SpecialContent("Special!")

  // 2 - actor has information about their context and about themselves
  // context.self = `this` in OOP

  case class SendMessageToYourSelf(content: String)

  simpleActor1 ! SendMessageToYourSelf("I am an actor")


  case class ForwardMessage(content: String)

  val alice = actorSystem.actorOf(Props[SimpleActor], "aliceActor")
  val bob = actorSystem.actorOf(Props[SimpleActor], "bobActor")

  bob tell(ForwardMessage("Hello!"), alice) // alice send bob a message


  /**
   * Exercise
   */

  case class IncrementMessage(value: Int)
  case class DecrementMessage(value: Int)

  class CounterActor extends Actor {
    private val value = 1000

    override def receive: Receive = onMessage(value)

    private def onMessage(value: Int): Receive = {
      case IncrementMessage(v) =>
        context.become(onMessage(value + v))
        println(s"[CounterActor] received IncrementMessage with value $v and total $value now")
      case DecrementMessage(v) =>
        context.become(onMessage(value - v))
        println(s"[CounterActor] received DecrementMessage with value $v and total $value now")
      case _ =>
    }
  }

  val counterActor = actorSystem.actorOf(Props[CounterActor], "CountActor")

  for (_ <- 1 to 100) {
    new Thread(() => counterActor ! IncrementMessage(1)).start()
  }

  for (_ <- 1 to 100) {
    new Thread(() => counterActor ! DecrementMessage(1)).start()
  }
}
