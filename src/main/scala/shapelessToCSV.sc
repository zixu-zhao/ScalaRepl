import shapeless._
import record._
import com.github.tototoshi.csv._
import play.api.libs.Files.SingletonTemporaryFileCreator


case class Unit(stringField: String)
case class unionCase(intField: Int, stringField: String, unit: Unit)

val caseClassToHlistOne = LabelledGeneric[unionCase].to(unionCase(1, "one", Unit("one")))
val caseClassToHlistTwo = LabelledGeneric[unionCase].to(unionCase(2, "two", Unit("two")))

val SeqOfHList = List(caseClassToHlistOne, caseClassToHlistTwo)

object toCSV extends Poly1 {
  implicit def caseInt = at[Int] { x => x.toString }

  implicit def caseOptionInt = at[Option[Int]] { x => x.getOrElse("").toString }

  implicit def caseString = at[String] { x => x }

  implicit def caseOptString = at[Option[String]] { x => x.getOrElse("") }

  implicit def caseTestClass = at[Unit] { case Unit(x) => x.toString }
}

val keys = SeqOfHList.map(k => k.keys.toList.map(_.name)).head

val content = SeqOfHList.map(s => s.values.map(toCSV).toList)

val temporaryCSVFile = SingletonTemporaryFileCreator.create()

val writer = CSVWriter.open(temporaryCSVFile)

writer.writeRow(keys)
writer.writeAll(content)