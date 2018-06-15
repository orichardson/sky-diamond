package diagrams.algebra

import breeze.linalg.DenseVector
import generic.Utils
import shapeless.ops.nat.ToInt
import shapeless.{AdditiveCollection, Nat, Sized, Succ}
import shapeless.ops.sized._
import spire.algebra.{CRing, Module}
import shapeless.nat._1

import scala.collection.{GenIterable, GenIterableLike, GenTraversable, IndexedSeqLike, SeqLike, mutable}
import scala.collection.generic.{CanBuildFrom, GenericCompanion, GenericTraversableTemplate, IsTraversableLike}
import scala.collection.mutable.ArrayBuffer
import scala.language.reflectiveCalls
import spire.std.seq.SeqModule
import generic.Utils.add_pipe_to_everything
import generic.Utils.{ImplicitOptions => I}

import scala.language.higherKinds
import scala.reflect.ClassTag


// note: the option here is generated by the `optionalImplicit` class.
sealed abstract class NVector[NT, N <: Nat : ToInt](implicit ringopt : Option[CRing[NT]]) extends NSeq[NT, N, ({type V[A] = NVector[A,N]})#V]
  with SeqLike[NT, NVector[NT, N]]
{
  def space(implicit ring: CRing[NT]) : Module[V, NT] = Module[V, NT]

  override def mk[T]: Sized[IndexedSeq[T], N] => NVector[T, N] = {
    import I.optionalImplicit
    NVector.make[T, N]
  }


  private def ringsolve(implicit preferred: CRing[NT] = null) : CRing[NT] = {
    Option(preferred).getOrElse(ringopt.getOrElse { throw new UnsupportedOperationException })
  }

  private def resolve( fun: Module[V, NT] => V )(rchance2 : Option[CRing[NT]]): NVector[NT, N] = {
    val r = ringsolve(rchance2.orNull)
    NVector.make( space(r) |> fun |> Sized.wrap[V, N] )( implicitly, Some(r) )
  }


  def +(other: NVector[NT, N])(implicit rc2 : CRing[NT] = null) : NVector[NT, N] = resolve( _.plus(vals, other.vals))(Option(rc2))
  def -(other: NVector[NT, N])(implicit rc2 : CRing[NT] = null) : NVector[NT, N] = resolve( _.plus(vals, other.vals))(Option(rc2))
  def *(other: NT)(implicit rc2 : CRing[NT] = null) : NVector[NT, N] = resolve( _.timesr(vals, other))(Option(rc2))

  def homoDVec(implicit ev : ClassTag[NT], rc2 : CRing[NT]) : DenseVector[NT] = new DenseVector[NT]( toArray ++ Array(rc2.one) )
  def toDVec(implicit ev : ClassTag[NT]) : DenseVector[NT] = new DenseVector[NT](vals.unsized.toArray)

  def colM(implicit ev : ClassTag[NT]): Mat[NT, N, _1] = Mat[NT, N, _1]( vals.unsized )
  def rowM(implicit ev : ClassTag[NT]): Mat[NT, _1, N] = Mat[NT, _1, N]( vals.unsized )

  def h_colM(implicit ev : ClassTag[NT], rc2 : CRing[NT] = null): Mat[NT, Succ[N], _1] =
    Mat[NT, Succ[N], _1]( vals.unsized ++ Array(ringsolve.one) )
  def h_rowM(implicit ev : ClassTag[NT], rc2 : CRing[NT] = null): Mat[NT, _1, Succ[N]] =
    Mat[NT, _1, Succ[N]]( vals.unsized ++ Array(ringsolve.one) )

  def mapNums[A : CRing[A]](f : NT => A) = NVector.make[A, N](vals.map(f))

}

/*abstract class NVecImpl[NT : CRing, N <: Nat : ToInt] extends NVector[NT, N] {
  override val space
}*/

object NVector  {
  //import generic.Utils.ImplicitOptions

  // def apply[NT: CRing, R[_], N <: Nat](sized: Sized[R[NT], N]): NVector[NT, N] { type Rep = R } =
  def wrap[NT, R[T] <: TraversableOnce[T], N <: Nat:ToInt](v : R[NT])
            (implicit ringopt : Option[CRing[NT]], cbf: CanBuildFrom[R[NT], NT, IndexedSeq[NT]]) : NVector[NT, N] =
    NVector.make(Sized.wrap[IndexedSeq[NT], N](v.toIndexedSeq))

  def wrapI[NT, N <:Nat:ToInt](v: IndexedSeq[NT])(implicit ringopt : Option[CRing[NT]]) : NVector[NT, N] = wrap[NT, IndexedSeq, N](v)

  def make[NT, N <: Nat : ToInt] (values: Sized[IndexedSeq[NT], N])(implicit ringopt : Option[CRing[NT]])
    : NVector[NT, N]  = new NVector[NT, N] {
    override def vals = values
  }

  def makeOpt[NT, N <: Nat : ToInt] (values: Option[Sized[IndexedSeq[NT], N]])(implicit ringopt : Option[CRing[NT]])
        : Option[NVector[NT, N] ] = values.map(s => make(s))

  implicit def makeSpace[NT : CRing, Dim <:Nat:ToInt] : Module[NVector[NT, Dim], NT] = new Module[NVector[NT,Dim], NT] {
    import I.optionalImplicit
    override def scalar: CRing[NT] = implicitly[CRing[NT]]
    override def timesl(r: NT, v: NVector[NT, Dim]): NVector[NT, Dim] = v * r
    override def negate(x: NVector[NT, Dim]): NVector[NT, Dim] = x.map( v => scalar.negate(v))
    override def zero: NVector[NT, Dim] = wrap(IndexedSeq.fill(ToInt[Dim].apply())( scalar.zero ))
    override def plus(x: NVector[NT, Dim], y: NVector[NT, Dim]): NVector[NT, Dim] = x + y
  }

  import shapeless.syntax.sized._
  private def mkbuilder[NT:CRing, N <: Nat:ToInt] = {
    import I.optionalImplicit;
    new ArrayBuffer[NT] mapResult (x => make(x.toIndexedSeq.sized[N].get))
  }

  implicit def canBuildFromChangeNT[NT : CRing, N <: Nat:ToInt]: CanBuildFrom[NVector[_, N], NT, NVector[NT, N]] =
    new CanBuildFrom[NVector[_, N], NT, NVector[NT, N]] {

      def apply(): mutable.Builder[NT, NVector[NT, N]] = mkbuilder[NT, N]
      def apply(from: NVector[_, N]): mutable.Builder[NT, NVector[NT, N]] = mkbuilder[NT, N]
    }



  def zeros[NT : CRing, N <: Nat:ToInt] : NVector[NT, N] = makeSpace[NT, N].zero

  //def apply[NT : CRing, N <: Nat] : NVector[NT, N]

  //implicit def seqnv[R : Ring](implicit )
}
