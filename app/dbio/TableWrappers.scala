package dbio

import java.time.Instant

import anorm.{Macro, RowParser, ~}
import generic.GenericSQL
import play.api.libs.json.{Json, Reads, Writes}
import shapeless.LabelledGeneric
import anorm.SqlParser.int


object TableWrappers {
  case class CellRow(name: String,
                      dim : Int,
                      pos: List[Double],
                      blade: List[Double],
                      mag: String,
                      sub: List[String],
                      sup : List[String])

  case class WRow(name: String,
                  descr: String,
                  modified: Instant,
                  thumburl: String )



  object CellRow {
    // this is code that could all be put in a macro.
    implicit val jreads : Reads[CellRow]  = Json.reads[CellRow]
    implicit val jwrites : Writes[CellRow] = Json.writes[CellRow]
    val parser : RowParser[CellRow] = Macro.namedParser[CellRow]
  }

  object WRow {
    // this is code that could all be put in a macro.
    implicit val jreads : Reads[WRow]  = Json.reads[WRow]
    implicit val jwrites : Writes[WRow] = Json.writes[WRow]
    val parser : RowParser[WRow] = Macro.namedParser[WRow]
  }
}
