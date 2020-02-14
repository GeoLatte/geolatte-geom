package org.geolatte.geom.circejson

import org.geolatte.geom.{Geometry, Position}
import org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84
import org.geolatte.geom.crs.LinearUnit
import org.geolatte.geom.generator.Generator
import org.specs2.matcher.MatchResult

class GeometryEncodersSpec extends org.specs2.mutable.Specification {

  import io.circe._
  import io.circe.syntax._
  import org.geolatte.geom.circe.GeoJsonCodec._

  import GeoJsonGen._

  "Encoding unit tests for 2D" >> {

    import org.geolatte.geom.syntax.GeometryImplicits._
    implicit val crs = WGS84
    val generatorSet = GeneratorSet((10.0, 10.0), (80.0, 80.0))

    "Encoding a point returns the same Json string as Jackson" >> {
      testGen(generatorSet.pointGen)
    }

    "Encoding a linestring returns the same Json string as Jackson" >> {
      testGen(generatorSet.lineGen)
    }

    "Encoding a polygon returns the same Json string as Jackson" >> {
      testGen(generatorSet.polyGen)
    }

    "Encoding a multipoint returns the same Json string as Jackson" >> {
      testGen(generatorSet.multiPointGen)
    }

    "Encoding a multiLineString returns the same Json string as Jackson" >> {
      testGen(generatorSet.multiLineGen)
    }

    "Encoding a multipolygon returns the same Json string as Jackson" >> {
      testGen(generatorSet.multiPolyGen)
    }

    "Encoding a geometrycollection returns the same Json string as Jackson" >> {
      testGen(generatorSet.geometryCollectionGen)
    }

  }

  def testGen[P <: Position](
      generator: Generator[_ <: Geometry[P]]): MatchResult[Any] = {
    val geom: Geometry[P] = generator.generate()
    val expected          = geom.asJsonString
    val received          = geom.asJson.pretty(Printer(true, "  "))

//    println(s"expected = $expected")
//    println(s"received = $received")

    expected must_== received
  }
}
