package org.geolatte.geom.circejson

import io.circe.parser.decode
import org.geolatte.geom.crs.CoordinateReferenceSystems._
import org.geolatte.geom.crs.{CoordinateReferenceSystems, CrsId}
import org.geolatte.geom._
import org.scalacheck.Gen
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

/**
  * Created by Karel Maesen, Geovise BVBA on 02/06/2020.
  */
class GeometryDecodersSpec extends Specification {

  import GeoJsonGen._
  import org.geolatte.geom.GeometryType._
  import org.geolatte.geom.circe.GeoJsonCodec._

  "Decoding CrsID" >> {
    val json =
      """
        |{"type":"name","properties":{"name":"EPSG:-1"}}
        |""".stripMargin
    val decoded = decode[CrsId](json)
    decoded must beRight(CrsId.UNDEFINED)
  }

  "Decoding Unit tests " >> {

    "Decoding a point GeoJson returns a point" >> {
      testScalaCheckGen(point2DGen)
      testScalaCheckGen(pointC2DGen)
      testScalaCheckGen(point3DGen)
      testScalaCheckGen(pointC3DGen)
      testEmpty(POINT)
    }

    "Decoding a linestring GeoJson returns a linestring" >> {
      testScalaCheckGen(line2DGen)
      testScalaCheckGen(lineC2DGen)
      testScalaCheckGen(line3DGen)
      testScalaCheckGen(lineC3DGen)
      testEmpty(LINESTRING)
    }

    "Decoding a polygon GeoJson returns a polygon" >> {
      testScalaCheckGen(polygon2DGen)
      testScalaCheckGen(polygonC2DGen)
      testScalaCheckGen(polygon3DGen)
      testScalaCheckGen(polygonC3DGen)
      testEmpty(POLYGON)
    }

    "Decoding a multipoint GeoJson returns a multipoint" >> {
      testScalaCheckGen(multiPoint2DGen)
      testScalaCheckGen(multiPointC2DGen)
      testScalaCheckGen(multiPoint3DGen)
      testScalaCheckGen(multiPointC3DGen)
      testEmpty(MULTIPOINT)
    }

    "Decoding a multilinestring GeoJson returns a multilinestring" >> {
      testScalaCheckGen(multiLine2DGen)
      testScalaCheckGen(multiLineC2DGen)
      testScalaCheckGen(multiLine3DGen)
      testScalaCheckGen(multiLineC3DGen)
      testEmpty(MULTILINESTRING)
    }

    "Decoding a multipolygon GeoJson returns a multipolygon" >> {
      testScalaCheckGen(multiPolygon2DGen)
      testScalaCheckGen(multiPolygonC2DGen)
      testScalaCheckGen(multiPolygon3DGen)
      testScalaCheckGen(multiPolygonC3DGen)
      testEmpty(MULTIPOLYGON)
    }

    "Decoding a GeometryCollection GeoJson returns a GeometryCollection" >> {
      testScalaCheckGen(geometryCollection2DGen)
      testScalaCheckGen(geometryCollectionC2DGen)
      testScalaCheckGen(geometryCollection3DGen)
      testScalaCheckGen(geometryCollectionC3DGen)
      testEmpty(GEOMETRYCOLLECTION)
    }

  }

  "Decoding with a user-specified default CRS" >> {
    implicit val gDecoder       = geometryDecoder(WEB_MERCATOR)
    val expected: Geometry[G2D] = point2DGen.sample.get
    val jsonString              = expected.asFasterXMLJsonString(false)
    val received                = decode[Geometry[_]](jsonString)
    received.map(_.getCoordinateReferenceSystem) must beRight(WEB_MERCATOR)
  }

  def testScalaCheckGen[P <: Position](generator: Gen[_ <: Geometry[P]]): MatchResult[Any] = {

    val expected: Geometry[P] = generator.sample.get
    val jsonString            = expected.asFasterXMLJsonString(true)
    val received              = decode[Geometry[_]](jsonString)
    received must beRight(expected)
  }

  def testEmpty(gtype: GeometryType): MatchResult[Any] = {
    val expected   = Geometries.mkEmptyGeometry(gtype, WGS84)
    val jsonString = expected.asFasterXMLJsonString(true)
    val received   = decode[Geometry[_]](jsonString)
    received must beRight(expected)
  }

}
