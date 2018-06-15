package diagrams.complexes

import io.TableWrappers
import io.TableWrappers.CellRow
import diagrams.algebra.NVector
import diagrams.complexes.GeoCell.{GeoCellSImpl, GeoCellZImpl}
import diagrams.complexes.Geometry.R2
import diagrams.complexes.NCell.{SCell, ZCell}
import play.Logger
import shapeless.Nat.{_0, _1, _2}
import shapeless.ops.nat.{Diff, ToInt}
import shapeless.ops.nat.Diff.Aux
import shapeless.{Nat, Succ, syntax}
import spire.syntax.ring._
import spire.implicits._

import generic.Utils.ImplicitOptions.optionalImplicit

trait Geometry[NT, MD <: Nat] {
  /*type Point <: GeoCell[NumType, _0, MaxDim]// with ZCell[NumType]
  type Segment <: GeoCell[NumType, _1, MaxDim]// with SCell[NumType, _0]
  type Area <: GeoCell[NumType, _2, MaxDim]// with SCell[NumType, _1] */

  //type Vec = NVector[Seq[NumType], MaxDim]

  type NumType = NT
  type MaxDim = MD

  def builtin_shapes : Seq[Shape]

  def dual[Dim <: Nat, ReverseDim <: Nat] (cell: GeoCell[NumType, Dim, MaxDim])
                        (implicit ev : Diff.Aux[MaxDim, Dim, ReverseDim]) : GeoCell[NumType, ReverseDim, MaxDim]

  def create_point(id: String, position: IndexedSeq[NumType]) : GeoCell[NumType, _0, MaxDim]
  def create_cell[PrevDim <: Nat:ToInt]( id : String, boundary: Seq[GeoCell[NumType, PrevDim, MaxDim]]) : GeoCell[NumType, Succ[PrevDim], MaxDim]
}




object Geometry {
  import shapeless.syntax.sized._

  object R2 extends Geometry[Double, _2] {
    /* These are to make the rest of the method more readable */
    type Pnt = GeoCell[Double, _0, _2]
    type Seg = GeoCell[Double, _1, _2]
    type Face = GeoCell[Double, _2, _2]


    override def builtin_shapes : Seq[Shape] = {
      Seq(new Shape.Rect[Double])
    }

    override def create_point(idd: String, pos: IndexedSeq[Double]): Pnt = new GeoCellZImpl[Double, _2] {
      override val id = idd
      override def magnitude = 1
      override def position = NVector.makeOpt(pos.sized[_2])
      override def direction = None
        //create_cell( "DUAL"+id, sup.map(g => g.dual) )//sup.map(g => g.dual)

    }

    override def create_cell[PrevDim <: Nat :ToInt](idd: String, border: Seq[GeoCell[Double, PrevDim, _2]]) : GeoCell[Double, Succ[PrevDim],_2]  =
        new GeoCellSImpl[Double, PrevDim, _2] {
      override val id = idd
      override def magnitude = ???
      override def position = None
      override def direction = ???
      override def boundary = border
    }

    override def dual[Dim <: Nat, ReverseDim <: Nat](cell: GeoCell[Double, Dim, R2.MaxDim])
                                                    (implicit ev: Aux[R2.MaxDim, Dim, ReverseDim]): GeoCell[R2.NumType, ReverseDim, R2.MaxDim] = ???
  }



/*  object StrictSquareLatice2D extends Geometry[Int, _2] {

    import syntax.sized._
    //val pt_iterator = Stream.from(0).iterator.map( i => new Vectr[Int,_2]((i % 10 :: i / 10 :: Nil).sized[_2].get))

    case class Point(override val id: String,
                     override val magnitude: Int = 1) extends GeoCell[Int, _0, _2] with ZCell[Int] {

      override val direction: Blade[Int, _0, _2] = new Blade[Int, _0, _2]()

      override def dual[ReverseDim](implicit ev: Diff.Aux[_2, _0, ReverseDim]): GeoCell[Int, ReverseDim,_2] = {
        new Area(id =this.id, )
      }

    }

    class Segment(override val id: String,
                  start: Point, end: Point,
                  override val magnitude: Int) extends GeoCell[Int, _1, _2] with SCell[Int, _0] {

      start.atch += Segment.this
      end.atch += Segment.this

      override val direction: Blade[Int, _1, _2] = ???

      override def boundary: Seq[NCell[Int, _0]] = start :: end :: Nil
    }

    class Area(override val id: String,
               top: Segment,
               right: Segment,
               bottom: Segment, left: Segment)
      extends GeoCell[Int, _2, _2] with SCell[Int, _1] {

      override val direction: Blade[Int, _2, _2] = new Blade[Int, _2, _2]() // oriented correctly
      override val magnitude = 1

      override def boundary: Seq[NCell[Int, _1]] = top :: right :: bottom :: left :: Nil
    }

  }*/

  def lookup (name : String) : Geometry[_,_ <: Nat] = name match {
    case "R2" => R2
    case _ => Logger.warn(s"Geometry '$name' not found; using R2 instead"); R2
  }

}