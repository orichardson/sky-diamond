package diagrams.algebra

import shapeless.{Nat, Sized}
import spire.algebra.Ring
import spire.algebra.VectorSpace

abstract class NVector[NT : Ring, N <: Nat] extends VectorSpace[Seq[NT], NT] {
  type Rep[T] = Seq[T]
  type V = Rep[NT]

  def create( values: Sized[Rep[NT], N]) : NVector[NT, N]
  def plus(a : NVector[NT, N], b : NVector[NT, N]): NVector[NT, N]

}

object NVector {
  def apply[NT: Ring, Rep[_], N <: Nat](implicit nv: NVector[NT, N]): NVector[NT, N] = nv

  //implicit def seqnv[R : Ring](implicit )
}
