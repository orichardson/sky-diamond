package diamond.core

// orientation = (x,y)
abstract class Direction[T] (implicit conv: Numeric[T]) {
  type Self /*>: this.type*/ <: Direction[T]
  def * (scale: T) : Self
  def * (scale: Int) : Self
}


object Direction {
  abstract class ConcreteDir[T, S <: ConcreteDir[T, _]]  (implicit conv: Numeric[T]) extends Direction[T] {
    override type Self = S
    def + (other: S) : S
    def - (other: S {type Self = S} ) : S = this + ( (other * -1) : S )
  }

  case class Dir2[T] (ori: (T,T)) (implicit conv: Numeric[T]) extends ConcreteDir[T, Dir2[T]]{
    import conv.mkNumericOps

    def + (other: Dir2[T]) : Dir2[T] = {
      Dir2[T](ori._1 + other.ori._1, ori._2 + other.ori._2)
    }

    def * (scale: T) : Dir2[T] = Dir2[T] ( (ori._1*scale, ori._2*scale))
    def * (scale: Int) : Dir2[T] = Dir2[T] ( (ori._1*conv.fromInt(scale), ori._2 * conv.fromInt(scale) ))

    def perp : Dir2[T] = Dir2[T]( (ori._2, -ori._1) )
  }

  type Dir2L = Dir2[Int]

  // particular cases to make this look nicer.
  /*val LDiag : Dir2L = Dir2L(1, 1)
  val RDiag = Dir2L(-1, 1)
  type Right extends Dir2L(1, 0)
  type Left extends Dir2L(-1, 0)
  type Up extends Dir2L(0, -1)
  type Down extends Dir2L(0, 1)*/
}