package diamond.core

import diamond.core.Direction.{ConcreteDir, Dir2L}
import shapeless.Nat._0

import scala.language.implicitConversions

abstract class Pos {
  type Self >: this.type <: Pos
  type DirType <: ConcreteDir[_, Self#DirType]  // DirType#Self == Self#DirType

  // DirType is bounded above (covariant) so it can't be an argument.
  // Need to give the type checker enough information to show that for some concrete type P,
  //  a : P#DirType  and b : P#DirType have

  def + (d : Self#DirType) : Self
  def inf (other:Self) : Self
  def sup (other:Self) : Self

  def interpolate (a : Self, b : Self) : (Double, Double)
}

object Pos {
  case class Pos2D[T] (x : T, y : T) (implicit conv: Numeric[T]) extends Pos {
    override type Self = Pos2D[T]
    override type DirType = Direction.Dir2[T]

    protected def make (x : T, y: T) : Self = Pos2D[T](x,y)

    // Note :this makes it easy to abstract to ND when I need to
    def meld(other:Pos2D[T], f : (T,T) => T ) : Self = make ( f(x, other.x), f(y,other.y) )

    override def + (d:Self#DirType#Self) : Self = make( conv.plus(x,d.ori._1), conv.plus(y, d.ori._2 ))
    override def inf (other:Self):Self = meld(other, conv.min)
    override def sup (other:Self):Self = meld(other, conv.max)

    override def interpolate(a: Self, b: Self): (Double, Double) = {
      import conv.mkNumericOps
      (conv.toDouble(x - a.x) / conv.toDouble(b.x - a.x), conv.toDouble(y - a.y) / conv.toDouble(b.y - a.y))
    }

  }

  type LaticePos2D = Pos2D[Int]
  type Point2D = Pos2D[Double]

  implicit def latice2point(lp: LaticePos2D) : Point2D = new Point2D(lp.x, lp.y)

}