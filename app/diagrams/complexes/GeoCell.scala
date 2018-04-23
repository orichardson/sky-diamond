package diagrams.complexes

import diagrams.complexes.NCell.{SCell, ZCell}
import shapeless.Nat.{_0, _1, _2}
import shapeless.ops.nat.Diff

import shapeless.{Nat, Succ, syntax}
import spire.algebra.Ring
import spire.syntax.ring._
import spire.implicits._


abstract class GeoCell[NumType, Dim <: Nat, MaxDim <: Nat](implicit numring: Ring[NumType]) extends NCell[NumType, Dim] {

 // def dual[ReverseDim](implicit ev : Diff[MaxDim, Dim] {type Out = ReverseDim}) : GeoCell[NumType, ReverseDim, MaxDim]
  val magnitude: NumType
  val direction: Blade[Dim, MaxDim]
}


/*trait GeoSCell[NumType, PrevDim, MaxDim] extends GeoCell[NumType, Succ[PrevDim], MaxDim] with SCell[NumType, PrevDim] {
  override def center: NVector[NumType, MaxDim] = boundary.reduce( _ + _ )
}*/


trait Geometry[NumType, MaxDim <: Nat] {
  type Point <: GeoCell[NumType, _0, MaxDim] with ZCell[NumType]
  type Stretch <: GeoCell[NumType, _1, MaxDim] with SCell[NumType, _0]
  type Area <: GeoCell[NumType, _2, MaxDim] with SCell[NumType, _1]
}


object StrictSquareLatice2D extends Geometry[Int, _2] {
  import syntax.sized._

  //val pt_iterator = Stream.from(0).iterator.map( i => new Vectr[Int,_2]((i % 10 :: i / 10 :: Nil).sized[_2].get))

  case class Point(override val id: String,
              override val magnitude:Int =1) extends GeoCell[Int, _0, _2] with ZCell[Int] {

    override val direction: Blade[_0, _2] = new Blade[_0, _2]()

  }

  class Stretch( override val id: String,
             start: Point, end: Point,
             override val magnitude: Int) extends GeoCell[Int, _1, _2] with SCell[Int, _0] {

    start.atch += Stretch.this
    end.atch += Stretch.this

    override val direction : Blade[_1, _2] = ???
    override def boundary: Seq[NCell[Int, _0]] = start :: end :: Nil
  }

  class Area(override val id: String,
             top: Stretch,
             right: Stretch,
             bottom: Stretch, left: Stretch)
              extends  GeoCell[Int, _2, _2] with SCell[Int, _1] {

    override val direction: Blade[_2, _2] = new Blade[_2, _2]() // oriented correctly
    override val magnitude = 1

    override def boundary: Seq[NCell[Int, _1]] = top :: right :: bottom :: left :: Nil
  }
}