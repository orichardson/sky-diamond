package diagrams.algebra

import diagrams.algebra.AbstractVal._

import spire.algebra.CRing
import spire.syntax.ring._

import Function.tupled
import scala.language.implicitConversions

abstract class AbstractVal[T] (implicit tring: CRing[T]) {
  val poly : Multinomial[T]

  def apply(feed: Label[T] => T) : T =
    (poly map tupled { (m: Monomial[T], coef : T) =>  coef * eval( m, feed) }) .reduce( tring.plus )

  override def toString: String = (poly.toSeq.sortBy( _._1.values.sum ) map tupled {
    (m : Monomial[T], c: T) => "("+c.toString +") "+
      (m.toSeq.sortBy(_._1.name) map tupled { (l: Label[T], e: Int) => l.name+ "^" + e }).mkString("  ")
  }).mkString("  +  ")
}



object AbstractVal {

  case class Label[T](name : String)(implicit num : CRing[T]) extends AbstractVal[T] {
    // actually don't need the implicit, just an identity but I'm hurried so I'll fix it later.
    override val poly: Map[Monomial[T], T] = Map(Map(this -> 1) -> num.one)
  }

  class AbsRing[T](implicit tring : CRing[T]) extends CRing[AbstractVal[T]] {
    override def negate(x: AbstractVal[T]) = AbstractVal(x.poly.mapValues(tring.negate))

    override val zero: AbstractVal[T] = AbstractVal[T](Map())
    override val one: AbstractVal[T] = AbstractVal(Map(Map(Label("x") -> 0) -> tring.one))

    override def plus(x: AbstractVal[T], y: AbstractVal[T]): AbstractVal[T] =
      merge(x.poly, y.poly, tring.plus, tring.zero).filter(_._2 != 0)

    override def times(x: AbstractVal[T], y: AbstractVal[T]): AbstractVal[T] = {
      for ((mx, coef_x) <- x; (my, coef_y) <- y) yield
        AbstractVal[T](Map(merge(mx, my, (_: Int) + (_: Int), 0) -> coef_x * coef_y ))
    }.reduce( plus ).filter(_._2 != 0)
  }

  object AbsRing {
    def apply[T](implicit tring : CRing[T]) = new AbsRing[T]
    implicit def getRing[T](implicit tring : CRing[T]) : CRing[AbstractVal[T]] = new AbsRing[T]
  }


  type Monomial[T] = Map[Label[T], Int] // maps labels to exponents
  type Multinomial[T] = Map[Monomial[T], T]


  def parse[T] (s : String)(implicit tring: CRing[T]): AbstractVal[T] = AbstractVal[T] ({
    val pattern = raw"((?:\([a-z]*\))|(?:[a-z]))(?:\^(\d*))?".r

    def clean_exp(s:String) = if(s == null) 1 else s.trim.toInt
    def clean_coef(s:String, i:Int) = if (i == 0) 1 else s.substring(0,i).trim.toInt

    //examples: x^2, 2*xy , 2xyz, 2 (hi)^2 (x)^2
    s.split("\\+" ).map(  mono => {
        val mo = mono.trim()
        val i = mo.indexWhere(c => !(c.isDigit || c.isWhitespace || c == '.'))

        if (i < 0)
          (Map[Label[T], Int](), tring.fromInt(mo.filterNot(_.isWhitespace).toInt))
        else
          (pattern.findAllMatchIn(mo.substring(i)).map( m => Label[T](m.group(1).trim()) -> clean_exp(m.group(2)) ).toMap,
              tring.fromInt(clean_coef(mo, i)) )
      })

  }.toMap)

  def merge[A,T] ( m1 : Map[A, T], m2: Map[A, T], combine : (T,T) => T, if_missing : T ): Map[A,T] =
    (m1.keySet | m2.keySet).iterator.map( k => k -> combine(m1.getOrElse(k, if_missing), m2.getOrElse(k, if_missing))).toMap

  def eval[T]( m: Monomial[T], feed: Label[T] => T )(implicit tring : CRing[T]) : T =
    (m map tupled { (label, power) => feed(label) ** power }).reduce( tring.times )

  def apply[T : CRing] (m: Multinomial[T]) : AbstractVal[T] = new AbstractVal[T] { override val poly = m }

  implicit def multinomial_of_absval[T : CRing](a : AbstractVal[T]) : Multinomial[T] = a.poly
  implicit def absval_of_multinomial[T : CRing](m : Multinomial[T]) : AbstractVal[T] = apply(m)
}
