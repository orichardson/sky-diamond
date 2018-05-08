package diagrams.complexes

import diagrams.algebra.AbstractVal
import play.twirl.api.Html
import shapeless.{Nat, Sized}


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
  type MaxDim <: Nat

  def name : String
  def thumbnail : Html // SVG

  def cell : GeoCell[AbstractVal[NumType], Dim, MaxDim]
  def concrete : Map[String, NumType] => GeoCell[NumType, Dim, MaxDim]
}

object Shape {
}
