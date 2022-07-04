import scala.util.Random
import scala.util.control.Exception.allCatch

def head[A](xs: List[A]): A = xs(0)

(1 to 10 by 2) toList

val x = (3, "Three", 3.0)

val upper = "yo, adrian".map{ c => c.toUpper }

val numPattern = "[0-9]+".r

"string"(0)

Random.alphanumeric.take(10).mkString

BigInt("1234567890123456789012345678901234567890")

def matchT(x: List[Int]): List[Int] = x match {
  case x @ List(1, _*) => x.take(1)
  case x @ List(2|3, _*) => x.take(2)
  case _ => x
}

matchT(List(1,2,3))
matchT(List(2,2,3))
matchT(List(3,2,3))
matchT(List(9,2,3))


allCatch.either("42".toInt)

sealed trait CrustSize
case object Small extends CrustSize
case object Medium extends CrustSize
case object Large extends CrustSize

sealed trait CrustType
case object Thin extends CrustType
case object Regular extends CrustType
case object Thick extends CrustType

class Pizza(var crustSize: CrustSize, crustType: CrustType) {
  def this(crustSize: CrustSize) = this(crustSize, Pizza.DefaultCrustType)
  def this(crustType: CrustType) = this(Pizza.DefaultCrustSize, crustType)
  def this() = this(Pizza.DefaultCrustSize, Pizza.DefaultCrustType)
}

object Pizza {
  val DefaultCrustSize = Medium
  val DefaultCrustType = Regular
}

trait TraitOne {
  def f1() = println("TraitOne F1")
  def f2() = println("TraitOne F2")
}

trait TraitTwo {
  def f3() = println("TraitTwo F3")
  def f4(): Unit
}

class ClassOne(val v: String) extends TraitOne with TraitTwo {
  override def f2(): Unit = println(s"ClassOne F2 $v")
  override def f4(): Unit = println(s"ClassOne F4 $v")
}

val c = new ClassOne("value")
c.f1()
c.f2()
c.f3()
c.f4()

trait Parent {
  def speak = "speak one"
}

trait GrandDad {
  def speak = "speak two"
}

class Speaker extends Parent with GrandDad {
  override def speak: String = super.speak

  def speakOne: String = super[Parent].speak
  def speakTwo: String = super[GrandDad].speak
}

val speaker = new Speaker()

println(speaker.speak)
println(speaker.speakOne)
println(speaker.speakTwo)

trait Stringify[A] {
  def toS(a: A): String
}

trait StringifyT {
  type A
  def toS(a: A): String
}

sealed trait Cat

class SmallCat extends Cat
class LargeCat extends Cat

trait Meow {
  type C <: Cat
  def meow(c: C): Unit
}

object SmallCat extends Meow {
  type C = SmallCat
  def meow(c: C): Unit = println("MEOW~~~~!!!")
}

object LargeCat extends Meow {
  type C = LargeCat
  def meow(c: C): Unit = println("gu")
}

val maomao = new SmallCat()
val gabi = new LargeCat()

SmallCat.meow(maomao)
LargeCat.meow(gabi)

trait AddService {
  def add(a: Int, b: Int): Int = a + b
}

trait MultiplyService {
  def multiply(a: Int, b: Int): Int = a * b
}

object MathService extends AddService with MultiplyService {}


def printAll(strings: String*): Unit = strings.foreach(println)

printAll()
printAll("a")
printAll("a", "b")
printAll("a", "b", "c")

val fruits = List("apple", "banana", "cherry")

printAll(fruits: _*)
