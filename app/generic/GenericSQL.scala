package generic

import java.sql.Connection

import anorm._
import shapeless._
import shapeless.ops.hlist.ToList
import shapeless.record._
import shapeless.ops.record._
import shapeless.syntax.singleton._
import GenericSQL.Q


// provides SQL methods for an A
trait GenericSQL[A] {
  def select(table_name : String, where : String="") : Q[List[A]]
  def insert(table_name : String, a : A) : Q[Option[Long]]
  def update[IdType](table_name : String, a : A, id: IdType) : Q[Boolean]

  // optional: delete
}

object GenericSQL {
  trait Q[Rslt] {

    def go(implicit conn : Connection) : Rslt
    def query : SimpleSql[Row]
    def string : String = toString
  }

  def apply[A](implicit genericSQL: GenericSQL[A]) : GenericSQL[A] = genericSQL

  implicit def generate[A, Rep <: HList, K <: HList, V <: HList]
              (implicit
               genAWit: LabelledGeneric.Aux[A, Rep],
               keys : Keys.Aux[Rep, K],
               ktl: ToList[K, Symbol],
               vals: Values.Aux[Rep, V]
              ) : GenericSQL[A]
  = new GenericSQL[A] {
    val names : Seq[String] = keys().toList.map(_.name)
    //val a_parser : RowParser[A] = ???

    override def select(table_name: String, where : String) : Q[List[A]] = new Q[List[A]] {
      def query = SQL"""
            SELECT $names FROM $table_name $where
         """

      override def go(implicit conn : Connection) : List[A] = ??? //query.as(a_parser.*)
      override def string = s"SELECT $names FROM $table_name $where"

    }

    override def insert(table_name: String, a: A) : Q[Option[Long]] = new Q[Option[Long]] {
      def query = {
        val r : Rep = genAWit.to(a)
        val vs = r.values
        SQL"""
            INSERT INTO $table_name VALUES (${vs.mkString("", ", " , "!")})
         """
      }

      override def string = s"INSERT INTO $table_name VALUES(${genAWit.to(a).values.mkString(" ", ", ", " ")})"

      override def go(implicit conn : Connection) : Option[Long] = query.executeInsert()
    }

    override def update[IdType](table: String, a: A, id: IdType): Q[Boolean]= ???
  }

}
