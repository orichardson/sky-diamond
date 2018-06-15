package diagrams.algebra

import shapeless.{Nat, Sized}
import shapeless.ops.nat.ToInt
import spire.algebra.CRing

import scala.collection.{SeqLike, mutable}
import scala.collection.generic.{GenericCompanion, GenericTraversableTemplate}
import scala.collection.mutable.ArrayBuffer

import scala.language.higherKinds

abstract class NSeq[A, N <: Nat:ToInt, Repr[T] <: NSeq[T, N, Repr]] extends Seq[A] with SeqLike[A, Repr[A]]
  with GenericTraversableTemplate[A, Repr] {

  //type V = Repr[A]
  type V = IndexedSeq[A]

  def vals : Sized[V, N]
  def mk[T] : Sized[IndexedSeq[T], N] => Repr[T]

  override def companion: GenericCompanion[Repr] = new GenericCompanion[Repr] {
    import shapeless.syntax.sized._
    override def newBuilder[AA]: mutable.Builder[AA, Repr[AA]] = new ArrayBuffer[AA] mapResult (x => mk(x.toIndexedSeq.sized[N].get))
  }
  override def length: Int = ToInt[N].apply()
  override def apply(idx: Int): A = vals.unsized.apply(idx)
  override def iterator: Iterator[A] = vals.unsized.toIterator
  override def seq: Seq[A] = vals.unsized.toSeq
}


object NSeq {

}