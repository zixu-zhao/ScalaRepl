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