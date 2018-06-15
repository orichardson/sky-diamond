package diagrams.complexes

import io.TableWrappers.CellRow
import diagrams.algebra.NVector
import diagrams.complexes.NCell.{SCell, ZCell}
import io.CellJS
import shapeless.ops.nat.{Diff, ToInt}
import shapeless.{Nat, Succ, _0, syntax}
import spire.algebra.CRing

// no need to introduce implicit here. I can always constrain this later, right?
// (implicit numring: spire.algebra.Ring[NumType])

trait GeoCell[NumType, Dim <: Nat, MaxDim <: Nat] extends NCell[NumType, Dim] { gc =>
  override final type CellType[D <: Nat] = GeoCell[NumType, D, MaxDim]
  //protected val vspace = NVector[NumType, Dim]

  //todo: figure out which of these can be vals instead.
  def magnitude: NumType
  protected[diagrams] val maxDimToInt : ToInt[MaxDim]

  // these are outer parameters (together with sup). They change depending on the context, and do not define
  // the cell itself. These are the ones that can change with translations, embeddings, etc...
  def position: Option[NVector[NumType, MaxDim]]
  def direction: Option[Blade[NumType, Dim, MaxDim]]

  def toCellJS(compressor : NumType => String) : CellJS = {
    CellJS(name = id,
      dim = dim,
      pos = position.map(_.vals.unsized.map(compressor).toList),
      blade = direction.map(_.data.map(compressor).toList),
      mag = magnitude.toString,
      sub = boundary.map(_.id).toList,
      sup = List(),
      extrajson = "{}",
      flipped = None)
  }

  def toCellRow(wid: Long)(implicit numeric : Numeric[NumType]) : CellRow  = {
    CellRow(name = id,
      dim = dim,
      pos = position.map(_.vals.unsized.map(numeric.toDouble).toList),
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

  def mapNums[A:CRing](f : NumType => A) : GeoCell[A, Dim, MaxDim] = GeoCell.mapNums(gc, f)
  def inject[NewMD <:Nat:ToInt](inj : NVector[NumType, MaxDim] => NVector[NumType, NewMD] )
    : GeoCell[NumType, Dim, NewMD] = GeoCell.inject(gc, inj)

}

object GeoCell {
  abstract class GeoCellZImpl[NT : CRing, MD <:Nat:ToInt] extends ZCell[NT] with GeoCell[NT, _0, MD] {
    override protected[diagrams] val maxDimToInt : ToInt[MD] = implicitly[ToInt[MD]]
  }
  abstract class GeoCellSImpl[NT : CRing, PD<:Nat:ToInt, MD <:Nat:ToInt] extends SCell[NT, PD] with GeoCell[NT,Succ[PD],MD] {
    override protected[diagrams] val maxDimToInt : ToInt[MD] = implicitly[ToInt[MD]]
  }


  def mapNums[A:CRing, NT, D <: Nat, MD <: Nat](gc : GeoCell[NT, D, MD], f : NT => A)
                                                           : GeoCell[A, D, MD] = new GeoCell[A, D, MD] {
    override type PrevDim = gc.PrevDim
    override protected[diagrams] implicit val tointD = gc.tointD
    override protected[diagrams] implicit val maxDimToInt = gc.maxDimToInt
    override protected[diagrams] val tointPrevD = gc.tointPrevD

    override val id = gc.id
    override val magnitude = f(gc.magnitude)
    override val position = gc.position.map(v => v.map(f)(NVector.canBuildFromChangeNT))
    override val direction = gc.direction.map(_.map(f))

    override val boundary : Seq[CellType[PrevDim]] = gc.boundary.map(_.mapNums[A](f))
  }

  def inject[NT, D <:Nat, MD1 <:Nat, MD2 <:Nat:ToInt]
          ( original : GeoCell[NT, D, MD1], inj : NVector[NT, MD1] => NVector[NT, MD2] ) : GeoCell[NT, D, MD2]
        = new GeoCell[NT, D, MD2] {
    override type PrevDim = original.PrevDim
    override protected[diagrams] val tointD = original.tointD
    override protected[diagrams] val tointPrevD = original.tointPrevD
    override protected[diagrams] val maxDimToInt = ToInt[MD2]

    override val id = original.id
    override val magnitude = original.magnitude
    override val position = original.position.map(inj)
    override val direction = ??? /// original.direction.map(_.inject(inj))

    override val boundary : Seq[CellType[PrevDim]] = original.boundary.map(_.inject(inj))
  }

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

  /*trait GCTemplate[NT,D <: Nat, MD <: Nat] {
    def repeat(n : Int)( code : GCTemplate[NT, D, MD] => GCTemplate[NT, D, MD])

    def build : GeoCell[NT, D, MD]
  }


  def maker[NT, D <: Nat, MD <: Nat] : GCTemplate[NT, D, MD] = ??? */
}


/*trait GeoSCell[NumType, PrevDim, MaxDim] extends GeoCell[NumType, Succ[PrevDim], MaxDim] with SCell[NumType, PrevDim] {
  override def center: NVector[NumType, MaxDim] = boundary.reduce( _ + _ )
}*/


