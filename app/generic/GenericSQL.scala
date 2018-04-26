package generic

import java.sql.Connection

import anorm._
import shapeless._
import shapeless.ops.hlist.{LeftFolder, ToList, ZipWithIndex}
import shapeless.record._
import shapeless.ops.record._
import shapeless.syntax.singleton._
import GenericSQL.Q
import anorm.SimpleSql.SimpleSqlShow
import anorm.SqlParser.long

// provides SQL methods for an A

trait GenericSQL[A] {
  def select(rest : String="") : Q[List[A]]
  def insert(a : A) : Q[Option[Long]]
  def updateRow[IdType](a : A, id: IdType) : Q[Int]
  def delete( rest : String) : Q[Unit]

  // secondary
  def select_with_id(where : String="") : Q[Map[Long, A]]
  def lookup[IdType] ( id : IdType) : Q[A]

  // optional: delete
}

object GenericSQL {
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
  implicit def generate[A, Rep <: HList, K <: HList, V <: HList, F <: HList]
          //implicit parameters all give information about the table itself.
              (implicit genAWit: LabelledGeneric.Aux[A, Rep],
               keys : Keys.Aux[Rep, K],
               ktl: ToList[K, Symbol],
               vals: Values.Aux[Rep, V],
               to_pairs: Fields.Aux[Rep, F],
               folder: LeftFolder.Aux[F, SimpleSql[Row], combine_sql.type, SimpleSql[Row]]
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
        override def query = genAWit.to(a).fields.foldLeft(
                  SQL(s"""
                        INSERT INTO $table_name ($names_str) VALUES (${names.map(n => s"{$n}").mkString(", ")})
                  """) : SimpleSql[Row] )( combine_sql )

      }

      //////**************** UPDATE QUERIES *************
      override def updateRow[IdType](a: A, id: IdType) = Q[Int] (
         s"UPDATE $table_name SET ($names_str) = (${
              genAWit.to(a).values.mkString(" ", ", ", " ")
            }) WHERE $id_col = $id",
        (q, conn) => q.executeUpdate()(conn)
      )

      ////******************* DELETE QUERIES *************
      override def delete(rest: String): Q[Unit] = Q[Unit] (
        s"DELETE FROM $table_name $rest",
        (q,conn) => q.execute()(conn)
      )
    }
  }

}
