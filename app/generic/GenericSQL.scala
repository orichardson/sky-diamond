package generic

import java.sql.Connection

import anorm._
import shapeless._
import shapeless.ops.hlist.{LeftFolder, Mapper, ToList, ZipWithIndex}
import shapeless.record._
import shapeless.ops.record._
import shapeless.syntax.singleton._
import GenericSQL.Q
import anorm.SimpleSql.SimpleSqlShow
import anorm.SqlParser.long
import generic.GenericSQL.fix_sql_repr.at
import play.Logger

import scala.reflect.ClassTag

// provides SQL methods for an A

trait GenericSQL[A] {
  def select(rest : String="") : Q[List[A]]
  def insert(a : A) : Q[Option[Long]]
  def updateRow[IdType](a : A, id: IdType) : Q[Int]
  def delete( rest : String) : Q[Unit]

  def updateField[T : ToParameterValue : ClassTag](colname: String, newVal : T, rest: String) : Q[Int]

  // secondary
  def select_with_id(where : String="") : Q[Map[Long, A]]
  def lookup[IdType] ( id : IdType) : Q[A]

  // optional: delete
}

object GenericSQL {
  type BoxedReal = java.lang.Float

  trait Q[Rslt] {

    def run(q : SimpleSql[Row])(implicit conn : Connection) : Rslt
    lazy val qstring : String = toString
    def query : SimpleSql[Row] = SQL(qstring)
      // this is unsafe, but easy. To make it secure, manually reimplement
      // your qstring method with interpolation. It's done this way for
      /// transparency and lack of bugs in development, so the code only exists in one
      // place, and you can see what the query is.
      // THe only time it's problematic is if you hand it unsanitized user input. So don't.

    def go(implicit conn : Connection) : Rslt = run(query)

  }

  object Q {
    def apply[Rslt](q : String, exec: (SimpleSql[Row], Connection) => Rslt)
                   : Q[Rslt] = new Q[Rslt] {
      override lazy val qstring = q
      override def run(sql: SimpleSql[Row])(implicit conn : Connection) = exec(sql, conn)
    }
  }

  trait PartialSQL[A] {
    def build(table_name: String, id_col: String, parser: RowParser[A] ) : GenericSQL[A]
  }

  // reason to keep the apply here, as opposed to providing arguments directly, is
  // that then the compiler won't fill in the type parameters [Rep, K, V], which I don't know
  // before running this. Instead, I can create a new kind of object.
  def apply[A](implicit genericSQL: PartialSQL[A]) : PartialSQL[A] = genericSQL


  trait base_id_map extends Poly1 { implicit def default[T] = at[T](identity)   }

  trait base_arrayify_map extends base_id_map {
    implicit def caseOtherList[T : ClassTag] = at[List[T]]( l => l.toArray )  }
  object fix_sql_repr extends base_arrayify_map {
    implicit def caseListDouble = at[List[Double]]( l => l.map(d => new BoxedReal(d)).toArray)
  }

  trait base_labelled_arrarify extends base_id_map {
    implicit def caseListOther[T : ClassTag, S <: Symbol] = at[(S, List[T])]( sl => (sl._1,  sl._2.toArray ) )
  }
  object fix_labelled_sql_repr extends base_labelled_arrarify {
    implicit def caseListDouble[S <: Symbol] = at[(S, List[Double])]( sl => (sl._1,  sl._2.map(d => new BoxedReal(d)).toArray ) )
  }


  object combine_sql extends Poly {
    implicit def write2Query[T : ToParameterValue, R <: Symbol]
    = use((so_far: SimpleSql[Row], tn: (R, T)) => so_far.on(tn._1 -> tn._2))
  }


  /**
    * I'm a fucking wizard.....
    *
    * @param genAWit: evidence that this type is a record of some sort
    * @param keys: evidence of the record keys
    * @param ktl: evidence I can turn the keys into a symbol list
    * @param vals: evidence of the value types
    * @param to_pairs: evidence that I can break my hlist into a certain kind of pairs
    * @param folder: evidence that the folding function above gives me a SimpleSQL query
    * @tparam A : the actual type I care about
    * @tparam Rep  : the type of the generic representation (HList)
    * @tparam K : the type of the keys (Hlist)
    * @tparam V : type of the values (HList)
    * @tparam F : type of the fields (HList)

    * @return a PartialSQL[A] for any type A! -- can be completed with a table name and id column
    *          to gain access to query generation methods detailed above.
    */
  implicit def generate[A, Rep <: HList, K <: HList, V <: HList, F <: HList, FFixed <: HList]
          //implicit parameters all give information about the table itself.
              (implicit genAWit: LabelledGeneric.Aux[A, Rep],
               keys : Keys.Aux[Rep, K],
               ktl: ToList[K, Symbol],
               vals: Values.Aux[Rep, V],
               to_pairs: Fields.Aux[Rep, F],
               connector: Mapper.Aux[fix_labelled_sql_repr.type, F, FFixed],
               folder: LeftFolder.Aux[FFixed, SimpleSql[Row], combine_sql.type, SimpleSql[Row]]
                                          ) : PartialSQL[A]
  = new PartialSQL[A] {
    var names : Seq[String] = keys().toList.map(_.name)
    // should be able to optionally set these.

    def build(table_name: String, id_col: String, parser: RowParser[A]) = new GenericSQL[A] {
      var names : Seq[String] = keys().toList.map(_.name)
      def names_str = names.mkString(", ")

      ///////*************** SELECT QUERIES *************
      // TODO: figure out why $names_str doesn't work in these select queries.
      override def select(rest: String) = new Q[List[A]] {
        override lazy val qstring = s"SELECT * FROM $table_name $rest"
        override def run(q : SimpleSql[Row])(implicit conn : Connection) = q.as(parser.*)
      }

      override def select_with_id(rest: String) = Q[Map[Long, A]] (
        s"SELECT * FROM $table_name $rest",
        (q, conn) => q.as( (long(id_col) ~ parser).* )(conn).map { case x ~ y => (x, y) } .toMap
      )

      override def lookup[IdType](id: IdType) = Q[A] (
        s"SELECT * FROM $table_name WHERE $id_col = $id",
        (q, conn) => q.as(parser.single)(conn)
      )

      //////**************** INSERT QUERIES *************
      override def insert(a: A) = new Q[Option[Long]] {
        override lazy val qstring = s"INSERT INTO $table_name VALUES(${genAWit.to(a).values.mkString(" ", ", ", " ")})"
        override def run(q: SimpleSql[Row])(implicit conn: Connection) = q.executeInsert()
        override def query = {
          genAWit.to(a).fields.map(fix_labelled_sql_repr).foldLeft(
            SQL(
              s"""INSERT INTO $table_name ($names_str) VALUES (${names.map(n => s"{$n}").mkString(", ")})"""
            ): SimpleSql[Row])(combine_sql)
        }

      }

      //////**************** UPDATE QUERIES *************
      override def updateRow[IdType](a: A, id: IdType) = new Q[Int] {
        override lazy val qstring = s"UPDATE $table_name SET ($names_str) = (${ genAWit.to(a).values.mkString(" ", ", ", " ")}) WHERE $id_col = $id"
        override def run(q: SimpleSql[Row])(implicit conn: Connection) =  q.executeUpdate()
        override def query ={
          genAWit.to(a).fields.map(fix_labelled_sql_repr).foldLeft(
            SQL(
              s"""UPDATE $table_name ($names_str) SET ($names_str) = (${names.map(n => s"{$n}").mkString(", ")}) WHERE $id_col = $id """
            ): SimpleSql[Row])(combine_sql)
        }
      }

      override def updateField[T : ToParameterValue : ClassTag](colname: String, newVal: T, rest: String) = new Q[Int] {
        override lazy val qstring = s"UPDATE $table_name SET ($colname) = (${ fix_sql_repr(newVal).toString }) $rest"
        override def run(q:SimpleSql[Row])(implicit conn:Connection) = q.executeUpdate
        override def query = {
          // wow this is dumb
          val dlist = TypeCase[List[Double]]
          val slist = TypeCase[List[String]]

          val fixed : ParameterValue = newVal match {
            case dlist(l) => fix_sql_repr(l)
            case slist(l) =>  l.toArray[String]
            case _ => newVal
          }

          SQL(s"UPDATE $table_name SET $colname = {col_value} $rest").on("col_value" -> fixed)
        }
      }


      ////******************* DELETE QUERIES *************
      override def delete(rest: String): Q[Unit] = new Q[Unit] {
        override lazy val qstring = s"DELETE FROM $table_name $rest"
        override def run(q: SimpleSql[Row])(implicit conn: Connection) = q.execute()
      }
    }
  }

}
