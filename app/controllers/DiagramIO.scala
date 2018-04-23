package controllers

import java.sql.Connection

import play.api.libs.json.{Json, Reads, Writes}
import play.api.db._
import play.api.mvc._
import javax.inject.Inject
import anorm.{Sql, _}
import shapeless.LabelledGeneric

import shapeless._
import shapeless.record._
import shapeless.ops.record._
import shapeless.syntax.singleton._

class DiagramIO @Inject()(db: Database, val controllerComponents: ControllerComponents)
  extends BaseController {

  case class CellInfo(name: String, dim : Int, pos: List[Double], blade: List[Double], mag: String, sub: List[String], sup : List[String])
  implicit val cellReads : Reads[CellInfo]  = Json.reads[CellInfo]
  implicit val cellWrites : Writes[CellInfo] = Json.writes[CellInfo]
  val cellinfoGen = LabelledGeneric[CellInfo]

  // still rather verbose, but

  private val cell_parser = Macro.namedParser[CellInfo].*

  def read_workspace(wid: Int) = Action { request =>
    var results : Map[String, Seq[CellInfo]] = db.withConnection { implicit conn : Connection =>
      SQL"SELECT * FROM  Cells c WHERE workspace=$wid".as(cell_parser).groupBy(_.dim)
        .map( d_v => (d_v._1+"-cells", d_v._2) )
    }

    Ok(Json.toJson(results))
  }

  def create_workspace = Action { request =>

    val name: String = request.getQueryString("name").getOrElse("[untitled]")
    val descr: String = request.getQueryString("descr").getOrElse("[no description]")

    db.withConnection { implicit conn =>
      SQL" INSERT INTO Workspaces(name, descr) VALUES($name, $descr)".executeInsert()
    } match {
      case Some(wid) => Ok("" + wid)
      case None => BadRequest(s"Could not insert ($name, $descr) into tables Workspaces")
    }
  }

  def new_cell(wid: Int) = Action(parse.json) { request =>
    db.withConnection { implicit conn =>

      val b = request.body.as[CellInfo]

      val cell_keys = Keys[cellinfoGen.Repr]
      val b_gen = cellinfoGen.to(b)

      val vals = implicitly[Values[cellinfoGen.Repr]]

      //// It really bothers me that I can't do this generically, but it's time to move on,
      // I've spent hours on this.

      SQL""" INSERT INTO Cells (name, descr) VALUES (${b_gen.values.toString}
             )
         """.executeInsert()
    } match {
      case Some(cid) => Ok("" + cid)
      case None => BadRequest("Could not insert  into tables Cells")
    }
  }


  def list = Action { request =>


    var results = db.withConnection { implicit conn : Connection =>

      SQL"SELECT name, descr FROM Workspaces".as(
          (SqlParser.str("name") ~ SqlParser.str("descr")).*)
        .map( pair => pair._1 + ": "+ pair._2)
        .mkString("\n")
    }

    Ok(results)
  }

  def apply_op(id: Int) = Action { request =>
    val json = request.body.asJson.get
    Ok
  }
}
