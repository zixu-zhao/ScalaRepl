import shapeless._
import record._
import com.github.tototoshi.csv._
import play.api.libs.Files.SingletonTemporaryFileCreator

/**
  * Create a sample case class
  *
  * @param stringField
  */
case class Unit(stringField: String)

case class unionCase(intField: Int, stringField: String, unit: Unit)

/**
  * Create two instances of the class we defined above
  */
val instanceOne = unionCase(1, "one", Unit("one"))
val instanceTwo = unionCase(2, "two", Unit("two"))

/**
  * Now we create a labelled generic TYPE of our sample case class
  *
  * In our case, labelledHList will be a TYPE that combine String,
  * Int, and Unit as a type list, with their fields' name
  */

val labelledHList = LabelledGeneric[unionCase]

/**
  * Now apply the instances to the heterogeneous list so we
  * have a heterogeneous list which values have dynamic type
  *
  * The heterogeneous list has a lot of useful methods just
  * like other linear sequence types in Scala -> List, Stream
  *
  * One could be map / filter
  */

val caseClassToHListOne = labelledHList.to(instanceOne)
val caseClassToHListTwo = labelledHList.to(instanceTwo)

/**
  * Also we could form the heterogeneous list back to case class
  *
  * So the return has type as unionCase
  */

val HListToCaseClass = labelledHList.from(caseClassToHListOne)


/**
  * Now suppose we have a sequence of heterogeneous lists, we could
  * transform it to the CSV/String format
  */

val SeqOfHList = List(caseClassToHListOne, caseClassToHListTwo)


/**
  * The object that inherits Poly1 in shapeless library requires a
  * interpreter for all types those in heterogeneous lists.
  *
  * The implicit parameters in the interpreter object aim to fit for
  * compilation, but implicit parameter is another topic
  */

object toCSV extends Poly1 {

  implicit def caseInt = at[Int] { x => x.toString }

  implicit def caseOptionInt = at[Option[Int]] { x => x.getOrElse("").toString }

  implicit def caseString = at[String] { x => x }

  implicit def caseOptString = at[Option[String]] { x => x.getOrElse("") }

  implicit def caseTestClass = at[Unit] { case Unit(x) => x.toString }
}

/**
  * Now we just use the toCSV object to interpret the mapping from
  * heterogeneous lists to string list, so for each element, compiler
  * will find an implicit parameter that fits the type of that element
  * to mapping it as a string.
  */
val content = SeqOfHList.map(s => s.values.map(toCSV).toList)


val keys = SeqOfHList.map(k => k.keys.toList.map(_.name)).head

val temporaryCSVFile = SingletonTemporaryFileCreator.create()

val writer = CSVWriter.open(temporaryCSVFile)

writer.writeRow(keys)
writer.writeAll(content)