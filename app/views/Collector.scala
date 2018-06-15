package views

import play.twirl.api.Html

import scala.collection.mutable
import scala.language.dynamics

class Collector extends Dynamic {
  val store = mutable.Map[String, mutable.Buffer[Html]]()

  def tagged(kind: String)(content: Html) : Unit = {
    if(!store.contains(kind))
      store(kind) = mutable.Buffer()

    store(kind) += content
  }

  def unravel(keys: String*): Html = Html(
    keys.map(k => "<!------------ "+k+"-------------->\n"+store.getOrElse(k, mutable.Buffer()).mkString("\n")).mkString("\n\n")
  )

  def applyDynamic(k : String)(content: Html) : Unit = tagged(k)(content)
  //def selectDynamic(k : String) : Html = unravel(k)
}