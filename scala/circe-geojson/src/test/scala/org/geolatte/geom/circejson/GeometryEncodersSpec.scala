package org.geolatte.geom.circejson

import org.geolatte.geom.{G2D, Geometry, Position}
import org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84
import org.geolatte.geom.crs.{Geographic2DCoordinateReferenceSystem, LinearUnit}
import org.geolatte.geom.generator.Generator
import org.scalacheck.Gen
import org.specs2.matcher.MatchResult

class GeometryEncodersSpec extends org.specs2.mutable.Specification {

  import io.circe._
  import io.circe.syntax._
  import org.geolatte.geom.circe.GeoJsonCodec._

  import GeoJsonGen._

  "Encoding unit tests" >> {

    import org.geolatte.geom.syntax.GeometryImplicits._
    implicit val crs: Geographic2DCoordinateReferenceSystem = WGS84

    "Encoding a point returns the same Json string as Jackson" >> {
      testScalaCheckGen(point2DGen)
      testScalaCheckGen(point3DGen)
    }

    "Encoding a linestring returns the same Json string as Jackson" >> {
      testScalaCheckGen(line2DGen)
      testScalaCheckGen(line3DGen)
    }

    "Encoding a polygon returns the same Json string as Jackson" >> {
      testScalaCheckGen(polygon2DGen)
      testScalaCheckGen(polygon3DGen)
    }

    "Encoding a multipoint returns the same Json string as Jackson" >> {
      testScalaCheckGen(multiPoint2DGen)
      testScalaCheckGen(multiPoint3DGen)
    }

    "Encoding a multiLineString returns the same Json string as Jackson" >> {
      testScalaCheckGen(multiLine2DGen)
      testScalaCheckGen(multiLine3DGen)
    }

    "Encoding a multipolygon returns the same Json string as Jackson" >> {
      testScalaCheckGen(multiPolygon2DGen)
      testScalaCheckGen(multiPolygon3DGen)
    }

    "Encoding a geometrycollection returns the same Json string as Jackson" >> {
      testScalaCheckGen(geometryCollection2DGen)
      testScalaCheckGen(geometryCollection3DGen)
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
