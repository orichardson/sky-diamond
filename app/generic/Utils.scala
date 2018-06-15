package generic

import java.time.{Duration, Instant}

import Function.tupled
import scala.language.implicitConversions

object Utils {
  // TIME

  def since(t : Instant) : String = format_duration(Duration.between(t, Instant.now))

  def format_duration(d : Duration, n_units : Int = 2): String = {
    val zipped =
      (d.toDays / 360, "years") ::
      (d.toDays % 360 , "days") :: (d.toHours % 24, "hours" ) ::
      (d.toMinutes % 60, "minutes") :: (d.getSeconds % 60, "seconds") :: Nil

    (zipped flatMap { case (amt, str) => if(amt > 0) Some(s"$amt $str") else None }).take(n_units).mkString(", ")
  }



  trait  LowPriorityOption{
    //implicit def none : Option[Nothing] = None
  }

  trait HasImplicitOptions extends LowPriorityOption {
    implicit def optionalImplicit[A <: AnyRef](implicit a: A = null): Option[A] = Option(a)
  }
  object ImplicitOptions extends HasImplicitOptions {
    def locally[T, R]( block: Option[T] => R)(implicit t : T = null): R = block(Option(t))
  }

  class Piping[A](a: A) { def |>[B](f: A => B) = f(a) }
  implicit def add_pipe_to_everything[A](a: A) : Piping[A] = new Piping(a)

}
