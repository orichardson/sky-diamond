package diagrams.complexes


import io.TableWrappers.CellRow
import shapeless.{Nat, Succ}
import shapeless.Nat.{_0, _1, _2}
import shapeless.ops.nat.ToInt

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

  protected[diagrams] implicit val tointPrevD : ToInt[PrevDim]
  protected[diagrams] implicit val tointD : ToInt[Dim]
  def dim : Int = tointD.apply()

  // keep track internally of all of the things that are built out of me; every time
  // the constructor of a (n+1)-cell is called, update this.
  var atch: mutable.MutableList[CellType[Succ[Dim]]] = mutable.MutableList()

  def boundary: Seq[CellType[PrevDim]]
  def attachments: Seq[CellType[Succ[Dim]]] = atch
}

object NCell {

  trait ZCell[NumType] extends NCell[NumType, _0] {
    type PrevDim = Nothing
    override protected[diagrams] val tointD : ToInt[_0] = ToInt[_0]
    override protected[diagrams] val tointPrevD : ToInt[PrevDim] = null
    override def boundary: Seq[CellType[Nothing]] = Nil
  }

  abstract class SCell[NumType, PrevD <: Nat:ToInt] extends NCell[NumType, Succ[PrevD]] {
    override type PrevDim = PrevD

    override protected[diagrams] val tointPrevD : ToInt[PrevD] = ToInt[PrevD]
    override protected[diagrams] val tointD : ToInt[Succ[PrevD]] = ToInt.toIntSucc[PrevD](tointPrevD)
  }
}