package diagrams.diamond.shapes

import diagrams.diamond.Pos


object DotShapes {
  class World[P <: Pos  {type Self = P}] (var dots: Map[P,Dot[P]]) {
    def apply (p: P): Dot[P] = dots(p)
  }

  /*implicit def map2world[P <: Pos  {type Self = P}](m : Map[P,Dot[P]]) : World[P] = new World(m)*/

  def ascii[P <: Pos  {type Self = P}] ( pts : Iterable[P] )(
              lex: P => Char = (p:P) => if (pts.iterator.contains(p)) '*' else ' ',
              resolution:Int = 50) : String = {
    val grid = Array.fill[Char](resolution, resolution)(' ')

    val top = pts.reduce(_ sup _)
    val bot = pts.reduce(_ inf _)

    pts.foreach { p =>
      val (x, y) = p.interpolate(bot, top)
      grid((x * resolution).toInt)((y * resolution).toInt) = lex(p)
    }

    grid.map(_.mkString).mkString("\n")
  }

  def carveLine[P <: Pos  {type Self = P}](start : P, dir : P#DirType,
                                           stop : P => Boolean, name: String = "carved" )  : DotLine[P] = {
    val failed = Iterator.from(0).find(i => stop(start + dir*i) )
    new DotLine[P](name, start, dir, failed.get)
  }




  trait View {
    type Shape
    def split: Seq[Shape]
  }
  trait V[S] extends View {override type Shape = S} // Sytnactic Sugar, constructor.

  trait DotShape[P <: Pos {type Self = P} ] extends Iterable[P] {
    def iso_class : String
    val name : String
    def views : Map[String, DotShapes.View]
    //def slice_Ps(dir: Direction): List[Shape]

    def strips (start: P, slice_dir: P#DirType, move_dir: P#DirType) : View  = new V[DotLine[P]] {
      override def split: Seq[DotLine[P]] =
        Iterator.from(0).map(i =>
              carveLine(start + move_dir*i, slice_dir, !iterator.contains(_ : P), s"strip$i")
          ).takeWhile(_.n >0 ).toSeq
    }

    def ascii : String = DotShapes.ascii(this)()
  }



  class Dot[P <: Pos {type Self = P}] (override val name : String, val loc: P) extends DotShape[P] {
    val iso_class : String = "*"
    //override def slice_Ps (dir: Direction): List[Shape] = List(this)
    override def iterator: Iterator[P] = List(loc).iterator
    override def views = Map()
  }

  class DotLine[P <: Pos {type Self = P}] (override val name :String,
                                           val start: P, val dir : P#DirType, val n : Int)
                                    extends DotShape[P]  {

    val places: IndexedSeq[P] = for( i <- 0 until n) yield start + (dir*i)

    override def iterator: Iterator[P] = places.iterator
    val iso_class : String = "LINE["+ n +"]"

    override def views = Map(
      "foreward" ->  new V[Dot[P]] { override def split = places.map( new Dot("", _) ) },
      "backward" ->  new V[Dot[P]] { override def split = places.reverse.map( new Dot("", _) ) }
    )
  }

  class Rect[P <: Pos {type Self = P} ](override val name : String,
                                        val start: P, val rows: Int, val rowdir: P#DirType,
                          val cols : Int, val coldir: P#DirType) extends DotShape[P] {

    val places: IndexedSeq[P] = for(r <- 0 until rows; c <- 0 until cols)
      yield start + rowdir * r + coldir * c

    override def iterator: Iterator[P] = places.iterator
    lazy val iso_class : String =  "RECT["+ Set(rows,cols).toString +"]"

    override def views = Map (
      "rows" ->  strips(start=start, slice_dir = rowdir, move_dir = coldir),

      "cols" ->  strips(start=start, slice_dir = coldir, move_dir = rowdir),
        /*new V[DotLine[P]] { override def split : Seq[Shape]
        = for(c <- 0 until cols) yield new DotLine[P] (start=start+coldir*c, dir=rowdir, n=rows)}*/

      "rdiag" -> strips(start, coldir - rowdir, coldir + rowdir),
      "ldiag" -> strips(start + coldir*cols, rowdir + coldir, rowdir - coldir)
    )
  }


  class Square[P <: Pos {type Self = P}](name :String, start: P,  size: Int,  rowdir: P#DirType, coldir: P#DirType)
                            extends Rect[P](name, start, size,rowdir,size, coldir) {
    override lazy val iso_class : String = "SQ["+ size +"]"
  }


  class Tri()


  // Rect
  // Tri
  // L
  // Row
  // Column
  // R_DIA
  // L_DIA
  // FRAME
  // TH-FRAME

  /*def parse[P <: Pos](pts: Set[P]) : DotShape[P] = {
    ???
  }*/
}
