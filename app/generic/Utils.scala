package generic

import java.time.{Duration, Instant}

import Function.tupled

object Utils {

  def since(t : Instant)  = format_duration(Duration.between(t, Instant.now))


  def format_duration(d : Duration, n_units : Int = 2): String = {
    val zipped =
      (d.toDays / 360, "years") ::
      (d.toDays % 360 , "days") :: (d.toHours % 24, "hours" ) ::
      (d.toMinutes % 60, "minutes") :: (d.getSeconds % 60, "seconds") :: Nil

    (zipped flatMap { case (amt, str) => if(amt > 0) Some(s"$amt $str") else None }).take(n_units).mkString(", ")
  }

}
