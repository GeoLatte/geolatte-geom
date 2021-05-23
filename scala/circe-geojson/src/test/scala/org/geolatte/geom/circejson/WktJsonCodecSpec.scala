package org.geolatte.geom.circejson

import io.circe
import io.circe.Decoder.Result
import org.geolatte.geom.circe.WktJsonCodec
import org.geolatte.geom.crs.CoordinateReferenceSystem
import org.geolatte.geom.{C2D, C3D, Geometry, LineString, Point, Position}
import org.scalacheck.Gen
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

/**
  * Specification for testing Geometries as JSON-embedded WKT strings
  */
class WktJsonCodecSpec extends Specification {

  import GeoJsonGen._
  import io.circe._
  import io.circe.syntax._
  import io.circe.parser.decode
  import org.geolatte.geom.circe.WktJsonCodec._

  import io.circe.generic.auto._, io.circe.syntax._
  case class TestGeom[P<: Position](geom: Geometry[P])

  "Encoding a Geometry returns a JSON String contain the WKT" >> {
    testScalaEncoding(line2DGen)
    //this verifies that we can also encode subtypes of Geometry
    testScalaPointEncoding(pointC3DGen)
    testScalaPointEncoding(point2DGen)
    testScalaLineStringEncoding(line2DGen)
    testScalaEncoding(geometryCollectionC3DGen)
  }

  "Decoding a Json WKT String returns a Geometry (in Lambert72 - 2D)" >> {

    implicit val crs: CoordinateReferenceSystem[C2D] = Lambert72C2D
    val expected = lineC2DGen.sample.get
    val wktString = s"""{"geom" : "${expected.asWktString(false)}"}"""
    val json = circe.parser.parse(wktString)
    val received = decode[TestGeom[C2D]](wktString)
    received must beRight(TestGeom(expected))
  }

  "Decoding a Json WKT String returns a Geometry (in Lambert72 - 3D)" >> {

    implicit val crs: CoordinateReferenceSystem[C3D] = Lambert72C3D
    val expected = lineC3DGen.sample.get
    val wktString = s"""{"geom" : "${expected.asWktString(false)}"}"""
    val json = circe.parser.parse(wktString)
    val received = decode[TestGeom[C3D]](wktString)
    received must beRight(TestGeom(expected))
  }

  def testScalaPointEncoding[P <: Position](generator: Gen[Point[P]]): MatchResult[Any] = {
    val geom: Point[P] = generator.sample.get
    val expected = geom.asWktString(false)
    val received = geom.asJson.as[String]
    received must beRight(expected)
  }


  def testScalaLineStringEncoding[P <: Position](generator: Gen[LineString[P]]): MatchResult[Any] = {
    val geom = generator.sample.get
    val expected = geom.asWktString(false)
    val received = geom.asJson.as[String]
    received must beRight(expected)
  }

  def testScalaEncoding[P <: Position](generator: Gen[_ <: Geometry[P]]): MatchResult[Any] = {
    val geom: Geometry[P] = generator.sample.get
    val expected = geom.asWktString(false)
    val received = geom.asJson.as[String]
    received must beRight(expected)
  }


}

