package diagrams.complexes

import diagrams.complexes.NCell.ZCell
import shapeless.ops.nat.Diff
import shapeless.{Nat, Succ, _0, syntax}


// no need to introduce implicit here. I can always constrain this later, right?
// (implicit numring: spire.algebra.Ring[NumType])

trait GeoCell[NumType, Dim <: Nat, MaxDim <: Nat] extends NCell[NumType, Dim] {
  override type CellType[D <: Nat] = GeoCell[NumType, D, MaxDim]
  //todo: figure out which of these can be vals instead.

  def magnitude: NumType
  def position: Seq[NumType]
  def direction: Blade[NumType]

  def dual[ReverseDim <: Nat] (implicit ev : Diff.Aux[MaxDim, Dim, ReverseDim]) : GeoCell[NumType, ReverseDim, MaxDim]

  // ******* provided: **********
  def sup : Seq[GeoCell[NumType, Succ[Dim], MaxDim]] = atch.toSeq
  /*def makeZCell(iid :String) : GeoNC[NumType, _0, MaxDim] = new ZCell[NumType]  {
    override type CellType[ D <: Nat ] = GeoCell[NumType, D, MaxDim]
    override val id :String  = iid
  }*/
}

object GeoCell {
  type Aux[N, D <: Nat, M <: Nat, R <: Nat] = GeoCell[N, D, M] {type  ReverseDim = R }


  //type GeoNC[NumType, Dim <: Nat, MaxDim <: Nat] = NCell[NumType, Dim] { type CellType[D <: Nat] <: GeoCell[NumType, D, MaxDim] }
}


/*trait GeoSCell[NumType, PrevDim, MaxDim] extends GeoCell[NumType, Succ[PrevDim], MaxDim] with SCell[NumType, PrevDim] {
  override def center: NVector[NumType, MaxDim] = boundary.reduce( _ + _ )
}*/


