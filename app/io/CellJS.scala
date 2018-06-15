package io

import play.api.libs.json.{Json, Reads, Writes}

case class CellJS(name: String,
                   dim : Int,
                   pos: Option[List[String]]/* = None */,
                   blade: Option[List[String]]/* = None*/,
                   mag: String,
                   sub: List[String]/* = List()*/,
                   sup : List[String]/* = List()*/,
                   extrajson : String /*= "{}"(*/,
                   flipped : Option[String] /*= None*/)

object CellJS {
  implicit val jreads: Reads[CellJS] = Json.reads[CellJS]
  implicit val jwrites: Writes[CellJS] = Json.writes[CellJS]

}