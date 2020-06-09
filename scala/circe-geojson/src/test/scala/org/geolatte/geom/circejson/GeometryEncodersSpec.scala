package org.geolatte.geom.circejson

import org.geolatte.geom.{Geometry, Position}
import org.scalacheck.Gen
import org.specs2.matcher.MatchResult

class GeometryEncodersSpec extends org.specs2.mutable.Specification {

  import GeoJsonGen._
  import io.circe._
  import io.circe.syntax._
  import org.geolatte.geom.circe.GeoJsonCodec._

  "Encoding unit tests" >> {

    "Encoding a point returns the same Json string as Jackson" >> {
      testScalaCheckGen(point2DGen)
      testScalaCheckGen(pointC2DGen)
      testScalaCheckGen(point3DGen)
      testScalaCheckGen(pointC3DGen)
    }

    "Encoding a linestring returns the same Json string as Jackson" >> {
      testScalaCheckGen(line2DGen)
      testScalaCheckGen(lineC2DGen)
      testScalaCheckGen(line3DGen)
      testScalaCheckGen(lineC3DGen)
    }

    "Encoding a polygon returns the same Json string as Jackson" >> {
      testScalaCheckGen(polygon2DGen)
      testScalaCheckGen(polygonC2DGen)
      testScalaCheckGen(polygon3DGen)
      testScalaCheckGen(polygonC3DGen)
    }

    "Encoding a multipoint returns the same Json string as Jackson" >> {
      testScalaCheckGen(multiPoint2DGen)
      testScalaCheckGen(multiPointC2DGen)
      testScalaCheckGen(multiPoint3DGen)
      testScalaCheckGen(multiPointC3DGen)
    }

    "Encoding a multiLineString returns the same Json string as Jackson" >> {
      testScalaCheckGen(multiLine2DGen)
      testScalaCheckGen(multiLineC2DGen)
      testScalaCheckGen(multiLine3DGen)
      testScalaCheckGen(multiLineC3DGen)
    }

    "Encoding a multipolygon returns the same Json string as Jackson" >> {
      testScalaCheckGen(multiPolygon2DGen)
      testScalaCheckGen(multiPolygonC2DGen)
      testScalaCheckGen(multiPolygon3DGen)
      testScalaCheckGen(multiPolygonC3DGen)
    }

    "Encoding a geometrycollection returns the same Json string as Jackson" >> {
      testScalaCheckGen(geometryCollection2DGen)
      testScalaCheckGen(geometryCollectionC2DGen)
      testScalaCheckGen(geometryCollection3DGen)
      testScalaCheckGen(geometryCollectionC3DGen)
    }

  }

  //TODO test decoding empty geometries

  def testScalaCheckGen[P <: Position](generator: Gen[_ <: Geometry[P]]): MatchResult[Any] = {
    val geom: Geometry[P] = generator.sample.get
    val expected          = geom.asFasterXMLJsonString(true)
    val received          = geom.asJson.printWith(Printer(dropNullValues = true, "  "))
    expected must_== received
  }

}
