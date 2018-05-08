package diagrams.complexes

import diagrams.algebra.NVector
import diagrams.complexes.NCell.{SCell, ZCell}
import shapeless.Nat.{_0, _1, _2}
import shapeless.ops.nat.Diff
import shapeless.{Nat, Succ, syntax}
import spire.syntax.ring._
import spire.implicits._

trait Geometry[N, MD <: Nat] {
  /*type Point <: GeoCell[NumType, _0, MaxDim]// with ZCell[NumType]
  type Segment <: GeoCell[NumType, _1, MaxDim]// with SCell[NumType, _0]
  type Area <: GeoCell[NumType, _2, MaxDim]// with SCell[NumType, _1] */

  //type Vec = NVector[Seq[NumType], MaxDim]

  type NumType = N
  type MaxDim = MD

  def builtin_shapes : Seq[Shape]

  def create_point(id: String, position: Seq[NumType]) : GeoCell[NumType, _0, MaxDim]
  def create_cell[PrevDim <: Nat]( id : String, boundary: Seq[GeoCell[NumType, PrevDim, MaxDim]]) : GeoCell[NumType, Succ[PrevDim], MaxDim]
}




object Geometry {
  object R2 extends Geometry[Double, _2] {
    override def builtin_shapes : Seq[Shape] = {
      Seq()
    }

    override def create_point(idd: String, pos: Seq[Double]): GeoCell[Double, _0, _2] = new GeoCell[Double, _0, _2] with ZCell[Double] {
      override val id = idd
      override def magnitude = 1
      override def position : Seq[Double] = pos
      override def direction = Blade.empty
      override def dual[ReverseDim <: Nat](implicit ev: Diff.Aux[_2, _0, ReverseDim]) : GeoCell[Double, ReverseDim, _2] = ???
        //create_cell( "DUAL"+id, sup.map(g => g.dual) )//sup.map(g => g.dual)
    }

    override def create_cell[PrevDim <: Nat](idd: String, boundary: Seq[GeoCell[Double, PrevDim, _2]]) : GeoCell[Double, Succ[PrevDim],_2]  =
        new GeoCell[Double, Succ[PrevDim],_2] with SCell[Double, PrevDim] {
      override val id = idd
      override def magnitude = ???
      override def position = ???
      override def direction = ???

      override def boundary = ???
      override def dual[ReverseDim <: Nat](implicit ev: Diff.Aux[_2, Succ[PrevDim], ReverseDim]) = ???


//      override def dual[ReverseDim <: Nat](implicit ev: Aux[_2, Succ[PrevDim], ReverseDim]) = ???
    }
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

  val test = 3
  def lookup (name : String) : Geometry[_,_ <: Nat] = name match {
    case "R2" => R2
  }

  def test2(i : Int) : Int = i

}