package org.geolatte.geom.circejson

import org.geolatte.geom.{Geometry, Position}
import io.circe.parser.decode
import org.geolatte.geom.crs.CrsId
import org.scalacheck.Gen
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
/**
  * Created by Karel Maesen, Geovise BVBA on 02/06/2020.
  */
class GeometryDecodersSpec extends Specification {


  import org.geolatte.geom.circe.GeoJsonCodec._

  import GeoJsonGen._

  "Decoding CrsID" >> {
    val json =
      """
        |{"type":"name","properties":{"name":"EPSG:-1"}}
        |""".stripMargin
    val decoded = decode[CrsId](json)(crsidDecoder)
    Right(CrsId.UNDEFINED)  must_== decoded
  }

  "Decoding Unit tests " >> {

    "Decoding a point GeoJson returns a point" >> {
      testScalaCheckGen(point2DGen)
      testScalaCheckGen(point3DGen)
    }

    "Decoding a linestring GeoJson returns a linestring" >> {
      testScalaCheckGen(line2DGen)
      testScalaCheckGen(line3DGen)
    }

    "Decoding a polygon GeoJson returns a polygon" >> {
      testScalaCheckGen(polygon2DGen)
      testScalaCheckGen(polygon3DGen)
    }

    "Decoding a multipoint GeoJson returns a multipoint" >> {
      testScalaCheckGen(multiPoint2DGen)
      testScalaCheckGen(multiPoint3DGen)
    }

    "Decoding a multilinestring GeoJson returns a multilinestring" >> {
      testScalaCheckGen(multiLine2DGen)
      testScalaCheckGen(multiLine3DGen)
    }

    "Decoding a multipolygon GeoJson returns a multipolygon" >> {
      testScalaCheckGen(multiPolygon2DGen)
      testScalaCheckGen(multiPolygon3DGen)
    }

    "Decoding a GeometryCollection GeoJson returns a GeometryCollection" >> {
      testScalaCheckGen(geometryCollection2DGen)
      testScalaCheckGen(geometryCollection3DGen)
    }

  }


  def testScalaCheckGen[P <: Position](generator: Gen[_ <: Geometry[P]]): MatchResult[Any] = {

    val expected: Geometry[P] = generator.sample.get
    val jsonString          = expected.asFasterXMLJsonString
    val received            = decode[Geometry[_]](jsonString)
    println(s"Received: $jsonString")
    Right(expected) must_== received
  }

}
