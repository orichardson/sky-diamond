package controllers

import java.sql.Connection
import java.time.Instant

import generic.GenericSQL
import javax.inject.Inject
import play.api.db.Database
import dbio.TableWrappers._
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import play.api.libs.json.{Json, Reads, Writes}
import anorm._
import play.api.Logger

class Application  @Inject()(db: Database, val cc: ControllerComponents)
  extends AbstractController(cc) {

  private lazy val w_sql = GenericSQL[WRow].build("Workspaces", "wid", WRow.parser)
  private lazy val c_sql = GenericSQL[CellRow].build("Cells", "cid", CellRow.parser)

  // DEBUGGING ROUTE
  def index_debug = Action {
    val top_cards = db.withConnection {  implicit c =>
      val q = w_sql.select("")
      Logger.debug( q.qstring)
      Logger.debug( q.query.toString )
      q.go
//      q.run( q.query )
//      q.query.as(WRow.parser.*)
//      SQL(" SELECT * FROM Workspaces ORDER BY modified DESC LIMIT 20").as(WRow.parser.*)(c)
    }


    Ok(top_cards.toString)

  }

  def index = Action {
    val top_cards = db.withConnection (
      w_sql.select_with_id("ORDER BY modified DESC LIMIT 20").go(_) )


    Ok(views.html.welcome(top_cards))
  }

  def display(wid : Long) = Action {
    val w = db.withConnection ( w_sql.lookup(wid).go(_) )
    Ok(views.html.display(w, wid))
  }

  def new_ws = Action { request =>
    val name: String = request.getQueryString("name").getOrElse("[untitled]")
    val descr: String = request.getQueryString("descr").getOrElse("[no description]")

    Logger.debug("RAW: "+ request.rawQueryString)
    Logger.debug(request.queryString.mkString(","))

    db.withConnection {
      w_sql.insert( WRow(name, descr, Instant.now, "")).go(_)
    } match {
      case Some(wid) => Ok(""+wid)
      case None => BadRequest(s"Could not insert ($name, $descr) into tables Workspaces")
    }
  }

  def get_cells(wid: Long) = Action {
    var results : Map[String, Seq[CellRow]] = db.withConnection( c_sql.select(s"WHERE workspace = $wid").go(_))
      .groupBy(_.dim).map( d_v => (d_v._1+"-cells", d_v._2) )

    Ok(Json.toJson(results))
  }

  def new_cell(wid: Long) = Action(parse.json) { request =>
    val b = request.body.as[CellRow]

    db.withConnection(c_sql.insert(b).go(_)) match {
      case Some(cid) => Ok(""+cid)
      case None => BadRequest(s"Could not insert `$b` into tables Cells")
    }
  }

  def update_cell(wid: Long) = Action(parse.json) { request =>
    val b = request.body.as[CellRow]
    val prev_name: String = request.getQueryString("previous_name").getOrElse(b.name)

    db.withConnection { implicit conn =>
      var keys = c_sql.select_with_id(s"WHERE workspace=$wid AND name=$prev_name").go.keySet
      assert(keys.size == 1, "Multiple names for same cell in Database")
      c_sql.updateRow(b, keys.head).go
    }

    Ok("Cell Updated")
  }

  def clear(wid:Int) = Action {
    db.withConnection(c_sql.delete(s"WHERE workspace=$wid").go(_)); Ok }



}
