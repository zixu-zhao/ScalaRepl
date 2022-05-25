import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.language.postfixOps

/**
 * Copyright nDimensional, Inc. 2015. All rights reserved.
 */
object Actor extends App {
  val system = ActorSystem("firstsystem")
  println(system.name)

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

  val wordCounter1 = system.actorOf(Props[WordCountActor], "wordCounter1")

  // part4 communicate with actor

  wordCounter1 ! "sending message 1"
  // asynchronous

  val wordCounter2 = system.actorOf(Props[WordCountActor], "wordCounter2")
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

  val personActor = system.actorOf(Person.props("zixu"), "PersonActor1")

  personActor ! "hi"


  case class SpecialContent(content: String)

  val Carol = system.actorOf(Props[SimpleActor], "carolActor")

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

  val simpleActor1 = system.actorOf(Props[SimpleActor], "SimpleActor1")

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

  val alice = system.actorOf(Props[SimpleActor], "aliceActor")
  val bob = system.actorOf(Props[SimpleActor], "bobActor")

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

  val counterActor = system.actorOf(Props[CounterActor], "CountActor")

  for (_ <- 1 to 10) {
    new Thread(() => counterActor ! IncrementMessage(1)).start()
  }

  for (_ <- 1 to 10) {
    new Thread(() => counterActor ! DecrementMessage(1)).start()
  }

  object FussyKid {
    case class KidAccept()
    case class KidReject()

    val HAPPY = "happy"
    val SAD = "sad"
  }
  
  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(message) => sender() ! (if (state == HAPPY) KidAccept else KidReject)
    }
  }

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive)
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) =>
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidReject
    }

  }

  object Mom {
    case class Start(kid: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)

    val VEGETABLE = "vegetable"
    val CHOCOLATE = "chocolate"
  }
  class Mom extends Actor {
    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case Start(kid) =>
        kid ! Food(VEGETABLE)
        kid ! Food(VEGETABLE)
        kid ! Ask("Do you want to play")
        kid ! Food(CHOCOLATE)
        kid ! Ask("Do you want to play")

      case KidAccept => println("Kid is happy")
      case KidReject => println("Kid is sad")
    }
  }

  val fussykid1 = system.actorOf(Props[FussyKid])
  val fussykid2 = system.actorOf(Props[StatelessFussyKid])
  val mom = system.actorOf(Props[Mom])

  mom ! Mom.Start(fussykid1)

  mom ! Mom.Start(fussykid2)

  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }
  class Counter extends Actor {
    import Counter._

    override def receive: Receive = count(0)

    def count(current: Int): Receive = {
      case Increment =>
        println(s"[counter] Increment - current: $current")
        context.become(count(current + 1))
      case Decrement =>
        println(s"[counter] Decrement - current: $current")
        context.become(count(current - 1))
      case Print =>
        println(s"[counter] Print - current: $current")
    }
  }

  val counter = system.actorOf(Props[Counter])

  (1 to 5) foreach(_ => counter ! Counter.Increment)
  (1 to 5) foreach(_ => counter ! Counter.Decrement)
  counter ! Counter.Print

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }
  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)

  }
  class Parent extends Actor {
    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} will create a child")
        val childActorRef = system.actorOf(Props[Child], name)
        context.become(withChild(childActorRef))
    }

    def withChild(ref: ActorRef): Receive = {
      case TellChild(message) => ref forward message
    }
  }

  val parentActor = system.actorOf(Props[Parent], "aParent")
  parentActor ! Parent.CreateChild("aChild")
  parentActor ! Parent.TellChild("hey!")

  /**
   * Akka system has three Guardian actors
   *  - / = the root Guardian
   *  - /system = system Guardian
   *  - /user = user Guardian
   *
   *  system and user sit under the root Guardian
   *  system Guardian manages user Guardian
   *  user Guardian manages all single actors users created
   */


  /**
   * Actor selection
   */

  val childSelection = system.actorSelection("/user/aChild")
  childSelection ! "I found you!"

  /**
   * NEVER PASS MUTABLE ACTOR STATE, or THE `THIS` REFERENCE, TO CHILD ACTORS
   */

  object NaiveBankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)

    case object InitializeAccount
  }
  class NaiveBankAccount extends Actor {
    import NaiveBankAccount._
    import CreditCard._

    var amount = 0
    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "aCreditCard")
        creditCardRef ! AttachToAccount(self)
      case Deposit(v) => deposit(v)
      case Withdraw(v) => withdraw(v)

    }

    def deposit(v: Int) = amount +=  v
    def withdraw(v: Int) = amount -= v
  }


  object CreditCard {
    case class AttachToAccount(bankAccount: ActorRef)

    case object CheckStatus
  }
  class CreditCard extends Actor {
    import CreditCard._

    override def receive: Receive = {
      case AttachToAccount(account) =>
        println(s"[credit card] ${self.path} Attach credit card to account ${account.path}")
        context.become(attachTo(account))
    }

    def attachTo(_account: ActorRef): Receive = {
      case CheckStatus =>
        println(s"${self.path} your message has been processed")
    }
  }

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount], "aNativeBankAccount")
  bankAccountRef ! NaiveBankAccount.InitializeAccount
  bankAccountRef ! NaiveBankAccount.Deposit(100)

  Thread.sleep(500)
  val creditCardRef = system.actorSelection("/user/aNativeBankAccount/aCreditCard")
  creditCardRef ! CreditCard.CheckStatus

}
