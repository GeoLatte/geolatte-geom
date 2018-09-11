package org.geolatte.geom.playjson

import org.geolatte.geom.crs.{CoordinateReferenceSystem, CrsId, CrsRegistry}
import org.geolatte.geom.{Points, _}
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.{Failure, Success, Try}


/**
  * @author Karel Maesen, Geovise BVBA
  *         creation-date: 7/31/13
  */


object GeometryJsonFormats {

  private def resolveCrs(id: CrsId): CoordinateReferenceSystem[_] =
    CrsRegistry.getCoordinateReferenceSystemForEPSG( id.getCode, null )

  implicit val crsIdReads: Reads[CrsId] = new Reads[CrsId] {
    override def reads(json: JsValue): JsResult[CrsId] = {
      Try {
            val epsg = (json \ "properties" \ "name").as[String]
            CrsId.parse( epsg )
          } match {
        case Success( crsId ) => JsSuccess( crsId )
        case Failure( e ) => JsError( e.getMessage )
      }
    }
  }

  implicit val crsReads: Reads[CoordinateReferenceSystem[_]] =
    crsIdReads.map( f => resolveCrs( f ) ).filter(
      ValidationError( s"Unknown CoordinateReferenceSystem" )
    )( _ != null )

  implicit val crsOptReads : Reads[Option[CoordinateReferenceSystem[_]]] = (__ \ "crs").readNullable[CoordinateReferenceSystem[_]]

  def crsReadsOrDefault(defaultCrs: CoordinateReferenceSystem[_]) : Reads[CoordinateReferenceSystem[_]] =
    crsOptReads.map( _.getOrElse(defaultCrs))


  private def createPoint[P <: Position](crs: CoordinateReferenceSystem[P], coordinates: Array[Double]): P = {
    if (coordinates.length == 2) {
      Positions.mkPosition(crs.getPositionClass, coordinates(0), coordinates(1))
    } else if (coordinates.length == 3) {
       Positions.
    } else {
      val z: Double = coordinates(2)
      val m: Double = coordinates(3)
      if (z.isNaN) {
        Points.create2DM(coordinates(0), coordinates(1), m, crsId)
      } else {
        Points.create3DM(coordinates(0), coordinates(1), z, m, crsId)
      }
    }
  }



  def pntReads(crs: CoordinateReferenceSystem[_]) : Reads[Point[_]] =
    (__ \ "coordinates").read[Array[Double]].map( arr => Geometries.mkPoint(Positions.))


  def mkGeometryReads(defaultCrs: CoordinateReferenceSystem[_]) = (
    crsReadsOrDefault(defaultCrs) and
      (__ \ "type").read[String] and
      __.json.pick
  )((crs, tpe, js) =>
      tpe.toLowerCase match {
        case "point" => js.as[Point[_]]( pntReads( crs ) )
      })



}

