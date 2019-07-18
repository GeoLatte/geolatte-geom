package org.geolatte.geom.circe

import org.geolatte.geom._
import org.geolatte.geom.syntax._
import GeometryImplicits._
import io.circe._
import io.circe.syntax._
import org.geolatte.geom.crs.CrsId
import org.geolatte.geom.{GeometryType, PositionSequence}

import scala.util.{Failure, Success, Try}

object GeoJsonCodec {

  import scala.collection.JavaConverters._

  implicit def encodePosition[P <: Position]: Encoder[P] = new Encoder[P] {
    override def apply(a: P): Json = {
      val ar = new Array[Double](a.getCoordinateDimension)
      a.toArray(ar)
      ar.asJson
    }
  }

  implicit def encodePositions[P <: Position]: Encoder[PositionSequence[P]] =
    new Encoder[PositionSequence[P]] {
      override def apply(poss: PositionSequence[P]): Json = {
        val css = poss.asScala.map(p => p.asInstanceOf[Position].asJson).toSeq
        Json.arr(css: _*)
      }
    }

  implicit val encodeCrs: Encoder[CrsId] = new Encoder[CrsId] {
    override def apply(crs: CrsId): Json =
      Json.obj("type"       -> Json.fromString("name"),
               "properties" -> Json.obj("name" -> Json.fromString(crs.toString)))
  }
//
//  implicit def encodeGeometry[P <: Position, G <: Geometry[P]]: Encoder[G] = new Encoder[G] {
//    override def apply(geom: G): Json =
//      Json.obj(
//        "type" -> Json.fromString("Point"),
//        "crs"  -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
//        "coordinates" -> (if (geom.getGeometryType == GeometryType.POINT) {
//                            geom.getPositionN(0).asJson
//                          } else geom.getPositions.asJson)
//      )
//
//  }

  implicit def encodePoint[P <: Position]: Encoder[Point[P]] = new Encoder[Point[P]] {
    override def apply(geom: Point[P]): Json =
      Json.obj(
        "type" -> Json.fromString("Point"),
        "crs"  -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
        "coordinates" -> geom.getPosition.asJson
        )

  }

}
