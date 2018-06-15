package diagrams.complexes

import io.TableWrappers.CellRow
import diagrams.algebra.{Mat, NVector, AbstractVal => Abstr}
import diagrams.algebra.AbstractVal._
import diagrams.complexes.GeoCell.{GeoCellSImpl, GeoCellZImpl}
import diagrams.complexes.NCell.ZCell
import io.CellJS
import play.twirl.api.Html
import shapeless.Nat._0
import shapeless.{Nat, Sized, Succ, nat}
import shapeless.nat.{_1, _2}
import shapeless.ops.nat.Diff.Aux
import shapeless.ops.nat.{LTEq, Pred, ToInt}
import spire.algebra.CRing
import spire.std.SeqModule
import spire.syntax.ring._

import generic.Utils.add_pipe_to_everything
import generic.Utils.ImplicitOptions.optionalImplicit

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

  val params : Set[Label[NumType]] // todo: HList, if we're doing this in a type-safe way.
  def name : String
  def thumbnail : Html // SVG
  val absmag: Abstr[NumType]
  val dim : Int

// def converters : List[Shape]
// def ops?

  def ideal: GeoCell[Abstr[NumType], Dim, Dim] // NCell?
  def concrete[MaxDim <: Nat:ToInt](newname: String, feed: Map[Label[NumType], NumType])
                             (embedding : Mat.Homo[NumType, MaxDim, Dim] )  : GeoCell[NumType, Dim, MaxDim]

  // def parse[MD](complex : GeoCell[NumType, Dim, MD])(implicit geom: Geometry[NumType, MD]) : Option[Map[String, NumType]]
  // need a build / fold / extrude
  // convenience
  private var counter = 0
  def withParam[MaxDim <: Nat:ToInt]( feed : (Symbol, NumType) *)
                              (pos: NVector[NumType, MaxDim] = null, blade : Blade[NumType, Dim, MaxDim] = null )
                              (implicit ring : CRing[NumType]): GeoCell[NumType, Dim, MaxDim] = {
    counter +=1
    concrete[MaxDim](
      name+"-"+counter.toString,
      feed.map( sn => (Label[NumType](sn._1.name)(ring), sn._2)).toMap)
    ()
  }

  def toCellRow(wid: Long): CellRow  = {
    CellRow(name = name,
      dim = dim,
      pos = None,
      blade = None,
      mag = absmag.toString,
      sub = ideal.boundary.map(_.id).toList,
      sup = List(),
      extrajson = "{}",
      flipped = None,
      workspace = wid)
  }

  def toJSCells : List[CellJS] = {
    ideal.boundary.map(_.toCellJS( x => x.toString )).toList
  }

}

object Shape {
  class Point[NT : CRing] extends Shape {
    override type NumType = NT
    override type Dim = _0
    override def thumbnail : Html =  Html(" NO IMPLEMENTATION ") // TODO: FINISH
    override val absmag: Abstr[NT] = one[NT]
    override val dim = 0

    override def name: String = "point"

    private val weight = Label[NT]("weight")
    override val params: Set[Label[NT]] = Set(weight)

    override def ideal : GeoCell[Abstr[NT], _0, _0] = new GeoCellZImpl[Abstr[NT], _0]  {
      override val id = name+"-proto"
      override def boundary = Seq()

      override def magnitude = absmag
      override def position = None
      override def direction = None
    }

    override def concrete[MaxDim <: Nat:ToInt](newname: String, feed: Map[Label[NT], NT])
                                        (pos: Option[NVector[NT, MaxDim]], blade: Option[Blade[NT, _0, MaxDim]]): GeoCell[NT, _0, MaxDim] =
      new GeoCellZImpl[NT, MaxDim] {
        override val id = newname
        override def magnitude = feed(weight)
        override def position = pos
        override def direction = blade
      }
  }

  abstract class ShapeImpl[NT : CRing, P <: Nat:ToInt] extends Shape {
    val vspace = new SeqModule[NT, IndexedSeq[NT]]()

    override type NumType = NT
    override type Dim = Succ[P]

    //this is ridiculous
    private implicit val dimtoi : ToInt[Dim] = ToInt.toIntSucc(implicitly[ToInt[P]])
    override val dim = dimtoi()

    override def thumbnail: Html = Html(" NO IMPLEMENTATION ") // TODO: finish
    def border : Seq[GeoCell[Abstr[NT], P, Dim]]

    override def ideal: GeoCell[Abstr[NT], Succ[P], Succ[P]] = new GeoCellSImpl[Abstr[NT], P, Succ[P]] {
      // silly things that should be automated.
      override type PrevDim = P
      override val id = name + "-proto"
      override def position = None //todo: average of components.
      override def direction = None // as a D-dimensional shape, the orientation is in S0, so is +/- 1

      // actual parts of the shape
      override def magnitude = absmag

      override def boundary: Seq[GeoCell[Abstr[NT], P, Succ[P]]] = border
    }

    override def concrete[MaxDim <: Nat:ToInt](newname: String, feed: Map[Label[NumType], NumType] )
                                        (embedding : Mat.Homo[NT, MaxDim, Dim] ) : GeoCell[NT, Dim, MaxDim]
      = new GeoCellSImpl[NT, P, MaxDim] {

      override def magnitude = absmag(feed)
      override def position = Some(ideal.position.map(_.mapNums(_(feed))).getOrElse(NVector.zeros[NT, Dim]) |> embedding )

      override def direction : Option[Blade[NT, Dim, MaxDim]] = Some( Blade.pseudoscalar[NT,Dim]().inject(embedding) )
      override val id = newname

      // boundary of concrete shape is: for each border element, apply the feed dict to all of the shape properties, and inject
      // it into the maximum dimension, according to possible orientations of the blade
      override def boundary : Seq[GeoCell[NT, PrevDim, MaxDim]] /* = border.map(g => g.mapNums(_.apply(feed)).inject[MaxDim]{
        v : NVector[NT, Dim] => direction.get.project(v) + position.get
      })*/ = ???
    }
  }

  object ShapeImpl {

  }

  class Seg[NT : CRing] extends ShapeImpl[NT, _0] {
    override val name = "segment"

    private val length = Label[NT]("length")
    override val params = Set(length)
    override val absmag = length

    override def border : Seq[GeoCell[Abstr[NT], _0, _1]] = {

      val p = new Point[Abstr[NT]]
      Seq(
        //todo: make this the centroid in the case of real numbers?
        p.withParam('weight -> Abstr.one)(blade = Blade.sca( -one[NT] ), pos = NVector.make( Sized( zero[NT] ) ) ),
        p.withParam('weight -> Abstr.one)(blade = Blade.sca( one[NT] ), pos = NVector.make( Sized(length) ) )
      )
    }
  }

  // example only; in general, these will live inside the database.

  // request
  class Rect[NT : CRing] extends ShapeImpl[NT, _1] {
    implicit val absr : CRing[Abstr[NT]] = Abstr.absRing[NT]

    override val name = "rectangle"

    // parameters.
    private val width = Label[NT]("width")
    private val height = Label[NT]("height")

    override val params = Set(width, height)
    override val absmag = absr.times(width, height) // width * height // should work...

    override def border: Seq[GeoCell[Abstr[NT], _1, _2]] = {
        val seg = new Seg[Abstr[NT]]
        val left = Blade.vec[Abstr[NT], _2]( Abstr.one, Abstr.zero )
        val up = Blade.vec[Abstr[NT], _2]( Abstr.zero, Abstr.one )

        Seq(
          seg.withParam('length -> width)(blade = left),
          seg.withParam('length -> height)(blade = up),
          seg.withParam('length -> width)(blade = -left ),
          seg.withParam('length -> height)(blade = -up )
        )
      }


    override def thumbnail: Html = Html(
      """ <rect width="100" height="100" style="fill:black;"></rect> """)

  }

}
