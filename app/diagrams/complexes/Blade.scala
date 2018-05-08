package diagrams.complexes

import shapeless.{Nat, _0}
import shapeless.nat._1

// https://en.wikipedia.org/wiki/Blade_(geometry)
class Blade[+NumType/*, Kind <: Nat, Max <: Nat*/](val data : Seq[NumType], val dim : Int) {
  // a representation of vectors, scalars, bi-vectors, etc.
}

object Blade {
  def empty[NumType] = new Blade[NumType](Seq(), 0)
  def vec[NumType](data: Seq[NumType]) = new Blade[NumType](data, 1)
}
