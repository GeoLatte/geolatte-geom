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

  //TODO -- is there a better way to do this?
  private[this] def dropCrs(js: Json): Json = return js.hcursor.downField("crs").delete.focus.get

  implicit def encodePoint[P <: Position]: Encoder[Point[P]] = new Encoder[Point[P]] {
    override def apply(geom: Point[P]): Json =
      Json.obj(
        "type"        -> Json.fromString("Point"),
        "crs"         -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
        "coordinates" -> geom.getPosition.asJson
      )
  }

  private def encodeLineStringCoordinates[P <: Position](geom: LineString[P]): Json =
    geom.getPositions.asJson

  implicit def encodeLineString[P <: Position]: Encoder[LineString[P]] =
    new Encoder[LineString[P]] {
      override def apply(geom: LineString[P]): Json =
        Json.obj(
          "type"        -> Json.fromString("LineString"),
          "crs"         -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
          "coordinates" -> encodeLineStringCoordinates(geom)
        )
    }

  private def encodePolygonCoordinates[P <: Position](geom: Polygon[P]): Json =
    Json.arr(geom.components().toVector.map(ring => ring.getPositions.asJson): _*)

  implicit def encodePolygon[P <: Position]: Encoder[Polygon[P]] = new Encoder[Polygon[P]] {
    override def apply(geom: Polygon[P]): Json =
      Json.obj(
        "type"        -> Json.fromString("Polygon"),
        "crs"         -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
        "coordinates" -> encodePolygonCoordinates(geom)
      )
  }

  implicit def encodeMultiPoint[P <: Position]: Encoder[MultiPoint[P]] =
    new Encoder[MultiPoint[P]] {
      override def apply(geom: MultiPoint[P]): Json =
        Json.obj(
          "type"        -> Json.fromString("MultiPoint"),
          "crs"         -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
          "coordinates" -> geom.getPositions.asJson
        )
    }

  implicit def encodeMultiLineString[P <: Position]: Encoder[MultiLineString[P]] =
    new Encoder[MultiLineString[P]] {
      override def apply(geom: MultiLineString[P]): Json =
        Json.obj(
          "type"        -> Json.fromString("MultiLineString"),
          "crs"         -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
          "coordinates" -> Json.arr(geom.components().map(encodeLineStringCoordinates): _*)
        )
    }

  implicit def encodeMultiPolygon[P <: Position]: Encoder[MultiPolygon[P]] =
    new Encoder[MultiPolygon[P]] {
      override def apply(geom: MultiPolygon[P]): Json =
        Json.obj(
          "type" -> Json.fromString("MultiPolygon"),
          "crs"  -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
          "coordinates" -> Json.arr(
            geom
              .components()
              .map(encodePolygonCoordinates): _*)
        )
    }

  implicit def encodeGeomColl[P <: Position]: Encoder[GeometryCollection[P]] =
    new Encoder[GeometryCollection[P]] {
      override def apply(geom: GeometryCollection[P]): Json =
        Json.obj(
          "type"       -> Json.fromString("GeometryCollection"),
          "crs"        -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
          "geometries" -> Json.arr(geom.components().map(g => dropCrs(g.asJson)): _*)
        )
    }

  implicit def encodeGeometry[P <: Position]: Encoder[Geometry[P]] = Encoder.instance {
    case g: Point[P]               => encodePoint(g)
    case l: LineString[P]          => encodeLineString(l)
    case p: Polygon[P]             => encodePolygon(p)
    case mp: MultiPoint[P]         => encodeMultiPoint(mp)
    case ml: MultiLineString[P]    => encodeMultiLineString(ml)
    case mpl: MultiPolygon[P]      => encodeMultiPolygon(mpl)
    case gc: GeometryCollection[P] => encodeGeomColl(gc)
    case _                         => sys.error("no encoder")
  }

}
