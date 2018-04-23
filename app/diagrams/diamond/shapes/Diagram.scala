package diamond

import diamond.core.Direction
import diamond.shapes.NCell.{OneCell, ZCell}

abstract class Diagram {
  val points: List[ZCell]
  val lines: List[OneCell]
}

object Diagram {
  object Orientations extends Enumeration {
    val Right, Left = Value
  }


  import Orientations._

  class Point(val dot: DotShapes.Dot[_])
  class Line(val from : Point, val to: Point, direction: Direction[_], val ori: Value = Right) {
  }
  class Area (val border: Seq[Line]) {
    for ((l1, l2) <- border zip border.tail) {
      require(l1.to == l2.from)
    }
  }
}