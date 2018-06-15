package controllers

import io.TableWrappers.{CellRow, WRow}
import generic.GenericSQL
import anorm._
import javax.inject.Inject
import play.api.db.Database
import play.api.mvc.{AbstractController, ControllerComponents}

class DebugCtrl @Inject()(db: Database, val cc: ControllerComponents)
  extends AbstractController(cc) {

  private lazy val w_sql = GenericSQL[WRow].build("Workspaces", "wid", WRow.parser)
  private lazy val c_sql : GenericSQL[CellRow] = GenericSQL[CellRow].build("Cells", "cid", CellRow.parser)

  def show_table(tabname: String) = Action { implicit request =>
    val rlst = try {
      val cols = db.withConnection { implicit c =>
        SQL"""select column_name from information_schema.columns where
        table_name=$tabname;""".as(SqlParser.str("column_name").*)
      }

      val data: List[List[String]] = db.withConnection[List[List[String]]] { implicit c: java.sql.Connection =>
        val ss = SQL("SELECT * FROM " + tabname.toLowerCase).map(r =>
          r.asList map {
            case x: Option[_] => x.getOrElse("---(empty)---").toString
            case x => x.toString
          })
        ss.as(ss.defaultParser.*)
      }
      Ok(views.html.debug.tabshow(tabname, cols, data))

    } catch {
      case e : Exception => Redirect("/debug/tables")//, message="could not read table '"+tabname+"'")
    }
    rlst
  }

  def list_tables = Action { implicit request =>
    val tabs: List[String] = db.withConnection { implicit c =>
      SQL("SELECT * FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';").as(SqlParser.str("tablename").*)
    }

    Ok(views.html.debug.tabindex(tabs))
  }

}

