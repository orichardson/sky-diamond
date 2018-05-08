package dbio

import java.time.Instant

import anorm.{Macro, RowParser, ToStatement, ~}
import generic.GenericSQL
import play.api.libs.json._
import shapeless._
import shapeless.ops.hlist.{At, LeftFolder, ToCoproduct, ToList, ZipWithIndex}
import shapeless.record._
import shapeless.ops.record._

object TableWrappers {
  type BoxedDouble = java.lang.Double

  case class CellRow(name: String,
                      dim : Int,
                      pos: List[Double],
                      blade: List[Double],
                      mag: String,
                      sub: List[String],
                      sup : List[String],
                      workspace : Long,
                      extrajson : String,
                      flipped : Option[String])

  case class WRow(name: String,
                  descr: String,
                  modified: Instant,
                  svgthumb: String,
                  geometry: String )



  object CellRow {
    // this is code that could all be put in a macro.
    implicit val jreads: Reads[CellRow] = Json.reads[CellRow]
    implicit val jwrites: Writes[CellRow] = Json.writes[CellRow]
    val parser: RowParser[CellRow] = Macro.namedParser[CellRow]

    private val cgen = LabelledGeneric[CellRow]
    private val cgkeys = Keys[cgen.Repr]
    private val toc = ToCoproduct[cgkeys.Out]

    class TC {
      type T
    }

    object TC {
      def apply[TT] = new TC {
        override type T = TT
      }
    }

    At[cgen.Repr, _0]

    /* Want
    for each (symbol, type) in case class definition

     */

    // this is all really dumb here I'm giving up
    def update_field(field: String, data: JsLookupResult, rest:String)(implicit csql: GenericSQL[CellRow]) = {
      field match {
        case "name" =>        csql.updateField(field, data.as[String], rest)
        case "dim" =>         csql.updateField(field, data.as[Int], rest)
        case "pos" =>         csql.updateField(field, data.as[List[Double]], rest)
        case "blade" =>       csql.updateField(field, data.as[List[Double]], rest)
        case "mag" =>         csql.updateField(field, data.as[String], rest)
        case "sub" =>         csql.updateField(field, data.as[List[String]], rest)
        case "sup" =>         csql.updateField(field, data.as[List[String]], rest)
        case "workspace" =>   csql.updateField(field, data.as[Long], rest)
        case "extrajson" =>   csql.updateField(field, data.as[String], rest)
        case "flipped" =>     csql.updateField(field, data.asOpt[String], rest)
      }
    }

    /*def update_field2(field: String, data: JsLookupResult, rest:String)(implicit csql: GenericSQL[CellRow]) = {

    }*/
  }

  object WRow {
    // this is code that could all be put in a macro.
    implicit val jreads : Reads[WRow]  = Json.reads[WRow]
    implicit val jwrites : Writes[WRow] = Json.writes[WRow]
    val parser : RowParser[WRow] = Macro.namedParser[WRow]
  }
}
