package diagrams

import dbio.TableWrappers.WRow
import diagrams.complexes.{Blade, Geometry, Shape}
import shapeless.Nat
import shapeless.ops.nat.LT

trait Workspace {
  type NumType
  type MaxDim <: Nat

  val wid : Long
  def row : WRow
  val geom : Geometry[NumType, MaxDim]
  def blades: Map[Int, List[Blade[NumType]]]
}

object Workspace {
  def from ( id: Long, wrow: WRow, already: List[Blade[_]] ) : Workspace = {
    val found_geom  = Geometry.lookup(wrow.geometry)

    type NT = found_geom.NumType
    type MD = found_geom.MaxDim

    new Workspace {
      override type NumType = NT
      override type MaxDim =  MD

      override val wid = id
      override val row = wrow
      override val geom : Geometry[NT, MD] = found_geom.asInstanceOf[Geometry[NT, MD]]

      override def blades = {
        already.asInstanceOf[List[Blade[NT]]].groupBy(b => b.dim)
      }
    }
  }
}
