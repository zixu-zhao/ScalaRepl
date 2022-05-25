import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

val incrementor = new Function1[Int, Int] {
  override def apply(v1: Int): Int = v1 + 1
}

case class Person(name: String) {
  def greet = s"HI, m name is $name"
}

implicit def fromStringToPerson(string: String): Person = Person(string)

"peter".greet

val aThread = new Thread(() => println("running"))
aThread.start()
aThread.join()


// scala Futures

val future = Future {
  // running on a different thread
  42
}

future.onComplete {
  case Success(value) => println(s"value is $value")
  case Failure(_) => println("failed")
}

val aProcessedFuture = future.map(_ + 1)
val aFlatFuture = future.flatMap { v => Future(v + 2)}

val forCompFuture = for {
  f1 <- aProcessedFuture
  f2 <- aFlatFuture
} yield f1 + f2

forCompFuture andThen {
  case Success(value) => println(value)
  case Failure(_) => println("Failure")
}

class BankAccount(private var amount: Int) {
  override def toString: String = amount.toString

  def withdraw(v: Int) = this.synchronized{ this.amount -= v }
  def deposit(v: Int) = this.synchronized{ this.amount += v }
}

val account = new BankAccount(2000)

for (_ <- 1 to 1000) {
  new Thread(() => account.withdraw(1)).start()
}

for (_ <- 1 to 1000) {
  new Thread(() => account.deposit(1)).start()
}

println(account.toString)



