package diamond.shapes


import shapeless.{Nat, Succ}
import shapeless.Nat.{_0, _1, _2}

/*
Want N-cells to be composed of (n-1) -cells. Getting the dependent types to work is quite difficult though.
In theory, the underscore could be replaced by the correct type, but I'm struggling to get the compiler to recognize
shapless Diff arithmetic properly, and I also can't name the argument to a successor since pattern matching doesn't work
in the type world...
 */
trait NCell[B <: Nat] {
  val id : String
  val pieces: Seq[NCell[_]]
}


object NCell {

  /*
  This is the equivalent of Dot, and should not require nearly the same kind of generics
   */

  trait ZCell extends NCell[_0]

  trait OneCell extends NCell[_1] {
    override val pieces : Seq[ZCell]
  }

  trait TwoCell extends NCell[_2] {
    override val pieces : Seq[OneCell]
  }
}