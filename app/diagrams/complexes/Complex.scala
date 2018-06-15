package diagrams.complexes

import shapeless.Nat
import spire.algebra.CRing

abstract class Complex[NT : CRing, Dim <: Nat](implicit geom: Geometry[NT, Dim]) {
  def cells[D <: Nat] : Seq[GeoCell[NT, D, Dim]]
}

object Complex {

}
