package diagrams.complexes


import shapeless.{Nat, Succ}
import shapeless.Nat.{_0, _1, _2}

import scala.collection.mutable

/*
Want N-cells to be composed of (n-1) -cells. Getting the dependent types to work is quite difficult though.
In theory, the underscore could be replaced by the correct type, but I'm struggling to get the compiler to recognize
shapless Diff arithmetic properly, and I also can't name the argument to a successor since pattern matching doesn't work
in the type world...
 */

import scala.language.higherKinds

trait NCell[NumType, Dim <: Nat] {
  val id : String

  type PrevDim <: Nat
  type CellType[D <: Nat] <: NCell[NumType, D]


  // keep track internally of all of the things that are built out of me; every time
  // the constructor of a (n+1)-cell is called, update this.
  var atch: mutable.MutableList[CellType[Succ[Dim]]] = mutable.MutableList()

  def boundary: Seq[NCell[NumType, PrevDim]]
  def attachments: Seq[NCell[NumType, Succ[Dim]]] = atch
}

object NCell {
  trait ZCell[NumType] extends NCell[NumType, _0] {
    type PrevDim = Nothing
    override def boundary: Seq[NCell[NumType, Nothing]] = Nil
  }

  trait SCell[NumType, PrevD <: Nat] extends NCell[NumType, Succ[PrevD]] {
    override type PrevDim = PrevD
  }
}