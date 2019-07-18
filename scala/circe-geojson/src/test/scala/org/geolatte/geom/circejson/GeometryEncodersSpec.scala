package org.geolatte.geom.circejson

import org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84


class GeometryEncodersSpec extends org.specs2.mutable.Specification {

  import io.circe._
  import io.circe.syntax._
  import org.geolatte.geom.circe.GeoJsonCodec._

  import GeoJsonGen._
  import org.geolatte.geom.types._
  import org.geolatte.geom.syntax.GeometryImplicits._
  implicit val crs = WGS84

  val generatorSet = GeneratorSet((10.0,10.0), (80.0, 90.0))


  "Encoding unit tests" >> {


    "Encoding a point returns the same Json string as Jackson" >> {
      val pnt = generatorSet.pointGen.generate
      val expected = pnt.asJsonString
      val received = pnt.asJson.pretty(Printer(true, true, "  "))
      expected must_== received
    }

  }

}
