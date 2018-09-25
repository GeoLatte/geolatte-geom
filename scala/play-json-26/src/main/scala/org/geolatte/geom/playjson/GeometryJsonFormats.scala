package org.geolatte.geom.playjson

import org.geolatte.geom._
import org.geolatte.geom.crs.{CoordinateReferenceSystem, CrsId, CrsRegistry}
import org.geolatte.geom.syntax.ArrayToPosition
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.{Failure, Success, Try}


/**
  * @author Karel Maesen, Geovise BVBA
  *         creation-date: 7/31/13
  */
object GeometryJsonFormats {

  import org.geolatte.geom.syntax.GeometryImplicits._

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
      JsonValidationError( s"Unknown CoordinateReferenceSystem" )
    )( _ != null )

  implicit val crsOptReads : Reads[Option[CoordinateReferenceSystem[_ <: Position]]] =
    (__ \ "crs").readNullable[CoordinateReferenceSystem[_ <: Position]]

  def crsReadsOrDefault(defaultCrs: CoordinateReferenceSystem[_]) : Reads[CoordinateReferenceSystem[_]] =
    crsOptReads.map( _.getOrElse(defaultCrs))


  object Helper extends ArrayToPosition



  def pntReads[Q <: Position](crs: CoordinateReferenceSystem[_]) : Reads[Point[Q]] =
    __.read[Array[Double]]
      .map( arr =>  {
          val pF = Helper.selectFactory[Q](crs, arr.length)
          Geometries.mkPoint( pF.make(arr), pF.crs)
      })

  def mkGeometryReads[ Q <: Position](defaultCrs: CoordinateReferenceSystem[_]): Reads[Geometry[Q]] = (
    crsReadsOrDefault(defaultCrs) and
      (__ \ "type").read[String] and
      (__ \ "coordinates").json.pick
  )( (crs, tpe, coordinates) => tpe.toLowerCase match {
        case "point" => coordinates.as[Point[Q]](pntReads(crs))
      })



}

