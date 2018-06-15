package diagrams.complexes

import diagrams.algebra.{Combinatorics, Mat, NVector}
import shapeless.{Nat, Succ, _0}
import shapeless.nat._1
import shapeless.ops.nat.ToInt
import spire.algebra.{CRing, Module, Rng}

import scala.collection.generic.{CanBuildFrom, GenericCompanion, GenericTraversableTemplate}
import scala.collection.{GenTraversable, SeqLike, mutable}
import scala.collection.mutable.{ArrayBuffer, Builder}
import scala.language.reflectiveCalls
import Function.tupled

// https://en.wikipedia.org/wiki/Blade_(geometry)
// a description of vectors, scalars, bi-vectors, etc.

class Blade[NumType: CRing, Kind <: Nat : ToInt, Max <: Nat : ToInt](val data : Seq[NumType])
    /*extends Seq[NumType] with SeqLike[NumType, Blade[NumType, Kind, Max]]
      with GenTraversable[NumType]
      with GenericTraversableTemplate[NumType, ({type B[A] = Blade[A, Kind,Max]})#B] */ {

  // def bases : NVector[NVector[NumType, Max], Kind] = ???

  val dim: Int = ToInt[Kind].apply()
  val maxdim : Int = ToInt[Max].apply()

  // ops, provided
  def inject[NewMax <: Nat : ToInt]( inj : NVector[NumType, Max] => NVector[NumType, NewMax] ) : Blade[NumType, Kind, NewMax] = {
    /*  inj :*/

    inj.
  }

  /*
    This is a K-blade in M-space. We're using it as a change of coordinates to take a K-vector within the
     blade to an M-vector in the outer space. This is a linear transformation usually done by a [M x K] matrix.
   */
  /*def project( v : NVector[NumType, Kind] ): NVector[NumType, Max] = {
    bases.zip(v.toSeq).foldLeft (NVector.zeros[NumType, Max]) { case (coll, (v, s)) => coll + (v*s) }
  }*/


  def map[A : CRing](f : NumType => A) : Blade[A, Kind, Max] = new Blade( data.map(f) )
  // filling requirements to be a SeqLike with the appropriate mapping structure.
  // override def companion: GenericCompanion[({type B[A] = Blade[A, Kind, Max]})#B] = new Blade.Comp[Kind, Max]
  //override def length: Int = data.length
  //override def apply(idx: Int): NumType = data.apply(idx)
  //override def iterator: Iterator[NumType] = data.iterator
  //override def seq: Seq[NumType] = data.seq
}

object Blade {//} extends GenericCompanion[({type B[A] = Blade[A, _, _]})#B] {
  // alias for readability.

  def sca[NumType : CRing, MD <: Nat:ToInt](data: NumType = implicitly[CRing[NumType]].one) = new Blade[NumType, _0, MD](Seq(data))
  def vec[NumType : CRing, MD <: Nat:ToInt](data: NumType*) = new Blade[NumType, _1, MD](data.toSeq)
  def pseudoscalar[NumType : CRing, MD <: Nat:ToInt](magnitude : NumType = implicitly[CRing[NumType]].one) = new Blade[NumType, MD, MD](Seq(magnitude))

  def mkbuilder[A:CRing, D<:Nat:ToInt, MD<:Nat:ToInt] : mutable.Builder[A, Blade[A, D, MD] ] = new ArrayBuffer[A] mapResult (x => new Blade[A,D,MD](x))

  implicit def canBuildFromChangeNT[NT:CRing, D<:Nat:ToInt, MD<:Nat:ToInt]: CanBuildFrom[Blade[_, D, MD], NT, Blade[NT, D, MD]] =
    new CanBuildFrom[Blade[_, D, MD], NT, Blade[NT, D, MD]] {
      def apply(): mutable.Builder[NT, Blade[NT, D, MD]] = mkbuilder[NT, D, MD]
      def apply(from: Blade[_, D, MD]): mutable.Builder[NT, Blade[NT, D, MD]] =  mkbuilder
    }

  // override def newBuilder[A]: mutable.Builder[A, Blade[A, _, _] ] = new ArrayBuffer[A] mapResult (x => new Blade[A, _, _](x))

  /*class Comp[K<:Nat:ToInt, M<:Nat:ToInt] extends GenericCompanion[({type B[A] = Blade[A, K, M]})#B] {
    override def newBuilder[A]: mutable.Builder[A, Blade[A, K, M] ] = ???  //new ArrayBuffer[A] mapResult (x => new Blade[A, K, M](x))
  }*/

  implicit def makeModule[NT : CRing, D <: Nat : ToInt, MD <: Nat : ToInt]: Module[Blade[NT,D,MD],NT] = new Module[Blade[NT,D,MD], NT] {
    override implicit def scalar: Rng[NT] = implicitly[CRing[NT]]

    override def timesl(r: NT, v: Blade[NT, D, MD]): Blade[NT, D, MD] = v.map( vi => scalar.times(r,vi) )
    override def negate(x: Blade[NT, D, MD]): Blade[NT, D, MD] = x.map( xi => scalar.negate(xi) )
    override def zero: Blade[NT, D, MD] = new Blade[NT, D, MD]( IndexedSeq.fill( Combinatorics.choose(ToInt[MD].apply(), ToInt[D].apply()).toInt )( scalar.zero ) )

    override def plus(x: Blade[NT, D, MD], y: Blade[NT, D, MD]): Blade[NT, D, MD] =
        { x.data.zip(y.data) map tupled ( (xi,yi) => scalar.times(xi, yi) ) }.to[({type B[A] = Blade[A, D,MD]})#B]
  }

}


/* class Blade[+NumType/*, Kind <: Nat, Max <: Nat*/](val data : Seq[NumType], val dim : Int)
    extends SeqLike[NumType, Blade[NumType]]
      with GenericTraversableTemplate[NumType, Blade]{

  // a representation of vectors, scalars, bi-vectors, etc.
  override def length: Int = data.length
  override def apply(idx: Int): NumType = data.apply(idx)
  override def iterator: Iterator[NumType] = data.iterator
  override def seq: Seq[NumType] = data
  override def companion : GenericCompanion[Blade] = Blade

  override protected[this] def newBuilder: mutable.Builder[NumType, Blade[NumType]]
      = new mutable.ArrayBuffer mapResult (x => new Blade(x, dim))


}

object Blade extends GenericCompanion[Blade] {
  // alias for readability.
  type Blder[Base] = mutable.Builder[Base, Blade[Base]]

  //override def empty[NumType] = new Blade[NumType](Seq(), 0)
  def vec[NumType](data: Seq[NumType]) = new Blade[NumType](data, 1)

  implicit def canBuildFrom[Base]: CanBuildFrom[Blade[_], Base, Blade[Base]] =
    new CanBuildFrom[Blade[_], Base, Blade[Base]] {
      def apply(): Blder[Base] = mkbuilder(0)
      def apply(from: Blade[_]): Blder[Base] =  mkbuilder(from.dim)
    }

  def mkbuilder[A](dimension: Int) : Blder[A] = new ArrayBuffer[A] mapResult (x => new Blade(x, dimension))
  override def newBuilder[A] : Blder[A] = mkbuilder(0)
} */