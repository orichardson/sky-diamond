package diagrams.algebra

import breeze.linalg._
import shapeless.{Nat, Succ}
import shapeless.ops.nat.ToInt

import scala.reflect.ClassTag


/*
Matrix
 */
class Mat[NT : ClassTag, A,B](val mat: Matrix[NT]) extends (NVector[NT,B] => NVector[NT,A]) {

  def *[C](other: Mat[NT, B,C]): Mat[NT, A,C] = new Mat[NT, A,C](mat*other.mat)
  def t: Mat[NT, B,A] = new Mat[NT, B,A]( mat.t.inner )
  def +(other: Mat[NT, A,B]): Mat[NT, A,B] = new Mat[NT, A,B](mat + other.mat)
  def :*(other: Mat[NT, A,B]): Mat[NT, A,B] = new Mat[NT, A,B](mat :* other.mat)
  def *(scalar: Double): Mat[NT, A,B] = new Mat[NT, A,B](mat * scalar)

  override def apply(v1: NVector[NT, B]): NVector[NT, A] = mat * v1.toDVec
  def homo = new Mat.Homo(mat)
}

object Mat {
  def apply[NT:ClassTag, R <: Nat:ToInt, C<:Nat:ToInt] ( vals : TraversableOnce[NT] ) : Mat[NT, R,C] = {
    val (rows, cols) = ( ToInt[R].apply(), ToInt[C].apply() )
    assert(rows * cols == vals.size)

    new Mat(new DenseMatrix[NT](rows, cols, vals.toArray))
  }


  class Homo[NT : ClassTag, R <: Nat, C <: Nat](mat :Matrix[NT]) extends Mat[NT , Succ[R], Succ[C]](mat) with (NVector[NT, C] => NVector[NT, R])
}