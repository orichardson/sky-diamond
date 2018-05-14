package diagrams.complexes

import dbio.TableWrappers.CellRow
import diagrams.algebra.NVector
import diagrams.complexes.NCell.ZCell
import shapeless.ops.nat.{Diff, ToInt}
import shapeless.{Nat, Succ, _0, syntax}


// no need to introduce implicit here. I can always constrain this later, right?
// (implicit numring: spire.algebra.Ring[NumType])

trait GeoCell[NumType, Dim <: Nat, MaxDim <: Nat] extends NCell[NumType, Dim] {
  override final type CellType[D <: Nat] = GeoCell[NumType, D, MaxDim]
  //protected val vspace = NVector[NumType, Dim]

  //todo: figure out which of these can be vals instead.
  def magnitude: NumType
  def position: Option[IndexedSeq[NumType]] // entirely for display purposes. A point cannot be displayed without one; same goes
      // for a line or plane without endpoints.
  def direction: Option[Blade[NumType]]

  def toCellRow(wid: Long)(implicit numeric : Numeric[NumType], dim2int : ToInt[Dim]) : CellRow  = {
    CellRow(name = id,
      dim = dim2int(),
      pos = position.map(_.map(numeric.toDouble).toList),
      blade = direction.map(_.data.map(numeric.toDouble).toList),
      mag = magnitude.toString,
      sub = boundary.map(_.id).toList,
      sup = List(),
      extrajson = "{}",
      flipped = None,
      workspace = wid)
  }


  // ******* provided: **********
  def sup : Seq[GeoCell[NumType, Succ[Dim], MaxDim]] = atch.toSeq
}

object GeoCell {

  /**
    * How to use in 2D, for 1d path:
    * GeoCell.maker[Double, _2, _2].bind('up -> (0,1), 'left -> (1,0)).path( +up, +left, -up, -left)
    *
    * works because from the previous point and direction you can infer a new point
    * in general this is not possible; from the area and orientation of a face, plus one segment, we don't know where
    * the rest of them are going to be. Therefore, it is necessary to specify the entire face.
    *
    * How to use in 3D, for 2D path
    * GeoCell.maker[Double, _3, _3].bind('up -> dual(0,0,1)  (, ...) {
    *   (up)
    * }
   */

  trait GCTemplate[NT,D <: Nat, MD <: Nat] {
    def repeat(n : Int)( code : GCTemplate[NT, D, MD] => GCTemplate[NT, D, MD])

    def build : GeoCell[NT, D, MD]
  }


  def maker[NT, D <: Nat, MD <: Nat] : GCTemplate[NT, D, MD] = ???
}


/*trait GeoSCell[NumType, PrevDim, MaxDim] extends GeoCell[NumType, Succ[PrevDim], MaxDim] with SCell[NumType, PrevDim] {
  override def center: NVector[NumType, MaxDim] = boundary.reduce( _ + _ )
}*/


