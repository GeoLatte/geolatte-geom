package org.geolatte.geom.playjson

import org.scalatest.FlatSpec
import org.geolatte.geom._
import org.geolatte.geom.crs.{CoordinateReferenceSystem, CoordinateReferenceSystems, CrsId}
import play.api.libs.json.{JsError, JsSuccess, Json}
import CoordinateReferenceSystems.WGS84

class GeometryReaderCRS extends FlatSpec {

  import GeometryImplicits._
  import GeometryJsonFormats._


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
