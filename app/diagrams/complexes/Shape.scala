package diagrams.complexes

import diagrams.algebra.{AbstractVal, NVector}
import diagrams.algebra.AbstractVal.{AbsRing, Label}
import diagrams.algebra.AbstractVal.AbsRing._
import play.twirl.api.Html
import shapeless.{Nat, Sized, Succ, nat}
import shapeless.nat.{_1, _2}
import shapeless.ops.nat.Diff.Aux
import shapeless.ops.nat.{LTEq, Pred, ToInt}
import spire.algebra.CRing
import spire.std.SeqModule
import spire.syntax.ring._

/**
  * A Shape bridges the abstract and concrete representations.
  *
  * In its most general form, a shape is just a geometric N-cell (i.e., has a boundary with blades),
  * that is closed. This is equivalent to the balancing condition in N-dimensions.
  *
  * Is a single abstract N-Cell, OR
  *   given a finite length, fixed-parameter, can produce a concrete shape with values
  *
  *  Also contains information about the natural cuts (i.e., views).
  */
trait Shape {
  type NumType
  type Dim <: Nat
  // type MyType <: ShapeInst

  val params : Set[String] // todo: HList, if we're doing this in a type-safe way.
  def name : String
  def thumbnail : Html // SVG

// def converters : List[Shape]
// def ops?

  def ideal: GeoCell[AbstractVal[NumType], Dim, Dim]
  def concrete[MaxDim <: Nat](newname: String, feed: Map[Label[NumType], NumType] )  : GeoCell[NumType, Dim, MaxDim]

  // def parse[MD](complex : GeoCell[NumType, Dim, MD])(implicit geom: Geometry[NumType, MD]) : Option[Map[String, NumType]]
  // need a build / fold / extrude
  // convenience
  private var counter = 0;
  def <<[MaxDim <: Nat]( feed : (Symbol, NumType) *)(implicit ring : CRing[NumType]): GeoCell[NumType, Dim, MaxDim]
  = {counter +=1; concrete[MaxDim](counter.toString, feed.map( sn => (Label[NumType](sn._1.name)(ring), sn._2)).toMap)}
}

object Shape {
  abstract class ShapeImpl[NT : CRing : Numeric, P <: Nat](implicit tip: ToInt[P]) extends Shape {
    val vspace = new SeqModule[NT, IndexedSeq[NT]]()

    override type NumType = NT
    override type Dim = Succ[P]

    val absmag: AbstractVal[NT]

    override def thumbnail: Html = ???
    def border : Seq[GeoCell[AbstractVal[NT], P, Dim]]

    override def ideal: GeoCell[AbstractVal[NT], Dim, Dim] = new GeoCell[AbstractVal[NT], Dim, Dim] {
      // silly things that should be automated.
      override val id = name+"-prototype"
      override type PrevDim = P
      override def position = None //todo: average of components.
      override def direction = None // as a D-dimensional shape, the orientation is in S0, so is +/- 1

      // actual parts of the shape
      override def magnitude = absmag
      override def boundary : Seq[GeoCell[AbstractVal[NT], PrevDim, Dim]] = border
    }

    override def concrete[MaxDim <: Nat](newname: String, feed: Map[Label[NumType], NumType] ): GeoCell[NT, Dim, MaxDim]
      = new GeoCell[NT, Dim, MaxDim] {
      override type PrevDim = P

      override def magnitude = absmag(feed)
      override def position = {
        val zv = IndexedSeq.fill[NT](1 + ToInt[P].apply())( implicitly[CRing[NT]].zero )

        val sum = boundary.flatMap(_.position).foldLeft( zv ) ( vspace.plus )
        Some(sum)// todo: divide.
      }
      override def direction = ???
      override val id = newname+ "["+name + "]"
      override def boundary : Seq[GeoCell[NT, PrevDim, MaxDim]] = ???
    }
  }

  object ShapeImpl {

  }

  // example only; in general, these will live inside the database.

  // request
  def rect[NT : CRing : Numeric] = new ShapeImpl[NT, _1] {

    override val name = "rectangle"
    override val params = Set("width", "height")
    override val absmag = AbstractVal.parse[NT]("(width)*(height)")

    override def border: Seq[GeoCell[AbstractVal[NT], _1, _2]] = {
        val seg : Shape { type NumType = AbstractVal[NT] } = ???
        Seq(
          seg << ('mag -> Label("width"))
        )
        ???
      }


    override def thumbnail: Html = Html(
      """ <rect width="100" height="100" style="fill:black;"></rect> """)

  }

}
