package org.geolatte.geom.playjson

import org.scalatest.FlatSpec
import org.geolatte.geom._
import org.geolatte.geom.crs.{CoordinateReferenceSystem, CoordinateReferenceSystems, CrsId, LinearUnit}
import play.api.libs.json.{JsError, JsSuccess, Json}
import CoordinateReferenceSystems.WGS84

class GeometryReaderCRS extends FlatSpec {

  import org.geolatte.geom.syntax.GeometryImplicits._
  import GeometryJsonFormats._
  import org.geolatte.geom.crs._



  "Crs objects" should "be parsed to CrsId" in {
    val json = Json.parse(JsonFragments.crsObject)

    val crs = json.as[CrsId]

    assertResult(crs)(CrsId.valueOf(4326))
  }

  "Crs objects" should "be parsed to a valid CoordinateReferenceSystem" in {
    val json = Json.parse(JsonFragments.crsObject)

    val crs = json.as[CoordinateReferenceSystem[_]]

    assertResult(crs)(WGS84)
  }

  "Crs objects" should "be parsed to a valid CoordinateReferenceSystem and cast to correct type" in {
    val json = Json.parse(JsonFragments.crsObject)

    val crs = json.as[CoordinateReferenceSystem[_]]
    assertResult(crs)(WGS84)
  }



  "Invalid Crs objects" should " fail when parsed " in {
    val json = Json.parse(JsonFragments.invalidCrsObject)

    val crs = json.validate[CrsId]
    assert( crs.isInstanceOf[JsError])
  }

  "Crs objects of non-existent CRS" should " fail when read as CoordinateReferenceSystem" in {
    val json = Json.parse(JsonFragments.nonExistentCrsObject)

    val crs = json.validate[CoordinateReferenceSystem[_]]
    assert( crs.isInstanceOf[JsError])
  }

  "Given a CRs, a GeometryReader" should "deserialize a Json Geometry " in {



    val json = Json.parse(JsonFragments.jsonPoint)
    val WGS84Z = WGS84.addVertical()
    implicit val geomReads = mkGeometryReads[Position](WGS84)
    val pnt = json.as[Geometry[Position]]

    val expected = point(WGS84Z)(1.0, 2.0, 3.0)

    assertResult( expected )(pnt)

  }



}



object JsonFragments {

  val crsObject =
    s"""
      |  {
      |    "type": "name",
      |    "properties": {
      |      "name": "${WGS84.getCrsId.toString}"
      |      }
      |    }
    """.stripMargin

  val jsonPoint =
    s"""
       |{ "crs" : $crsObject,
       |  "type" : "Point",
       |  "coordinates": [1.0,2.0, 3.0]
       |  }
     """.stripMargin

  val invalidCrsObject =
    s"""
       |  {
       |    "type": "name",
       |    "properties": {
       |      "name": "EPSG:abc"
       |      }
       |    }
    """.stripMargin

  val nonExistentCrsObject =
    s"""
  {
       |    "type": "name",
       |    "properties": {
       |      "name": "EPSG:10"
       |      }
       |    }
       |
     """.stripMargin



}
