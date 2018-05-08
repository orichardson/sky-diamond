package diagrams.algebra

import shapeless.{Nat, Sized}
import spire.algebra.Ring

trait NVector[Rep, N <: Nat] extends Ring[Rep] {

  def create( values: Sized[Rep, N]) : NVector[Rep, N]

  def plus(a : NVector[Rep, N], b : NVector[Rep, N]): NVector[Rep, N]

}

object NVector {
  //def apply[NumType, N]( vals : NumType*) : NVector[NumType, N] = new NVector[NumType, N]( Sized.vals.toSeq)
}
