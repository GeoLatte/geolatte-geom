package org.geolatte.geom.circe

import io.circe.Decoder.Result
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import org.geolatte.geom.codec.{Wkt, WktDecoder, WktEncoder}
import org.geolatte.geom._
import org.geolatte.geom.crs.CoordinateReferenceSystem

import scala.util.Try


object WktJsonCodec {

  def encoder: WktEncoder = Wkt.newEncoder(Wkt.Dialect.SFA_1_2_1)

  def decoder: WktDecoder = Wkt.newDecoder(Wkt.Dialect.SFA_1_2_1)

  def encode[P <: Position, G <: Geometry[P]] : Encoder[G]= (geom) => {
    Json.fromString(encoder.encode(geom))
  }

  implicit def pntEncoder[P <: Position]: Encoder[Point[P]] = encode[P, Point[P]]
  implicit def lstEncoder[P <: Position]: Encoder[LineString[P]] = encode[P, LineString[P]]
  implicit def plgEncoder[P <: Position]: Encoder[Polygon[P]] = encode[P, Polygon[P]]
  implicit def mPntEncoder[P <: Position]: Encoder[MultiPoint[P]] = encode[P, MultiPoint[P]]
  implicit def mLsEncoder[P <: Position]: Encoder[MultiLineString[P]] = encode[P, MultiLineString[P]]
  implicit def mPlgEncoder[P <: Position]: Encoder[MultiPolygon[P]] = encode[P, MultiPolygon[P]]
  implicit def gcEncoder[P <: Position]: Encoder[GeometryCollection[P]] = encode[P, GeometryCollection[P]]
  implicit def geomEncoder[P <: Position]: Encoder[Geometry[P]] = encode[P, Geometry[P]]

  implicit def geomDecoder[P <: Position]
  (implicit crs: CoordinateReferenceSystem[P]): Decoder[Geometry[P]] =
    new Decoder[Geometry[P]] {
      override def apply(c: HCursor): Result[Geometry[P]] = for {
        wkt <- c.as[String]
        result <- Try {
          decoder.decode(wkt, crs)
        }.fold[Result[Geometry[P]]](
          t => Left(DecodingFailure(s"Attempt to decode value on failed cursor: $t", c.history)),
          g => Right(g)
        )
      } yield result
    }

}
