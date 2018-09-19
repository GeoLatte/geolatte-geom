package org.geolatte.geom.playjson

import org.geolatte.geom._
import org.geolatte.geom.crs.{CoordinateReferenceSystem, CrsId, CrsRegistry}
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.{Failure, Success, Try}


/**
  * @author Karel Maesen, Geovise BVBA
  *         creation-date: 7/31/13
  */
object GeometryJsonFormats {

  private def resolveCrs(id: CrsId): CoordinateReferenceSystem[_ <: Position]  =
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

  implicit val crsReads: Reads[CoordinateReferenceSystem[_ <: Position]] =
    crsIdReads.map( f => resolveCrs( f ) ).filter(
      ValidationError( s"Unknown CoordinateReferenceSystem" )
    )( _ != null )

  implicit val crsOptReads : Reads[Option[CoordinateReferenceSystem[_ <: Position]]] =
    (__ \ "crs").readNullable[CoordinateReferenceSystem[_ <: Position]]

  def crsReadsOrDefault(defaultCrs: CoordinateReferenceSystem[_]) : Reads[CoordinateReferenceSystem[_]] =
    crsOptReads.map( _.getOrElse(defaultCrs))



//  def posReads[P <: Position, Q <: P](crs: CoordinateReferenceSystem[P]) : Reads[Q] =
//    __.read[Array[Double]].map(arr =>  arr.length match {
//      case 2 =>
//    })


//  def pntReads(crs: CoordinateReferenceSystem[_ <:Position]) : Reads[Point[_ <:Position]] =
//    (__ \ "coordinates").read[Array[Double]]
//      .map( arr =>
//              Geometries.mkPoint( Positions.mkPosition(crs.getPositionClass, arr:_*),crs)
//      )
//
//
//  def mkGeometryReads(defaultCrs: CoordinateReferenceSystem[_ <: Position]) = (
//    crsReadsOrDefault(defaultCrs) and
//      (__ \ "type").read[String] and
//      __.json.pick
//  )((crs, tpe, js) =>
//      tpe.toLowerCase match {
//        case "point" => js.as[Point[_]]( pntReads( crs ) )
//      })



}

