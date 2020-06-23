package org.geolatte.geom.circe

import io.circe._
import io.circe.syntax._
import org.geolatte.geom.GeometryType._
import org.geolatte.geom.crs.CoordinateReferenceSystems._
import org.geolatte.geom.crs.{CoordinateReferenceSystem, CrsId, CrsRegistry}
import org.geolatte.geom.{PositionSequence, _}

import scala.util.Try

trait CrsIdCodec {
  val crsEncoderByName: Encoder[CrsId] = (crs: CrsId) =>
    Json.obj(
      "type"       -> Json.fromString("name"),
      "properties" -> Json.obj("name" -> Json.fromString(crs.toString))
  )

  val crsDecoderByName: Decoder[CrsId] = (c: HCursor) =>
    for {
      crsId <- c.downField("properties").downField("name").as[CrsId](epsgDecoder)
    } yield crsId

  val epsgDecoder: Decoder[CrsId] = Decoder.decodeString.emapTry { str =>
    Try(CrsId.parse(str))
  }
}

trait GeometryCodec {

  import scala.jdk.CollectionConverters._

  implicit def encodePosition[P <: Position]: Encoder[P] = (a: P) => {
    val ar = new Array[Double](a.getCoordinateDimension)
    a.toArray(ar)
    ar.asJson
  }

  implicit def encodePositions[P <: Position]: Encoder[PositionSequence[P]] =
    (poss: PositionSequence[P]) => {
      val css = poss.asScala.map(p => p.asInstanceOf[Position].asJson).toSeq
      Json.arr(css: _*)
    }

  private[this] def dropCrs(js: Json): Json = js.hcursor.downField("crs").delete.focus.get

  def assembleSimpleGeom(gtype: GeometryType, crs: Json, coordinates: Json): Json =
    if (crs.isNull) {
      Json.obj("type" -> Json.fromString(gtype.getCamelCased), "coordinates" -> coordinates)
    } else
      Json.obj("type"        -> Json.fromString(gtype.getCamelCased),
               "crs"         -> crs,
               "coordinates" -> coordinates)

  implicit def encodePoint[P <: Position](implicit crsE: Encoder[CrsId]): Encoder[Point[P]] =
    (geom: Point[P]) =>
      assembleSimpleGeom(geom.getGeometryType,
                         geom.getCoordinateReferenceSystem.getCrsId.asJson,
                         geom.getPosition.asJson)

  private def encodeLineStringCoordinates[P <: Position](geom: LineString[P]): Json =
    geom.getPositions.asJson

  implicit def encodeLineString[P <: Position](
      implicit crsE: Encoder[CrsId]): Encoder[LineString[P]] =
    (geom: LineString[P]) =>
      assembleSimpleGeom(geom.getGeometryType,
                         geom.getCoordinateReferenceSystem.getCrsId.asJson,
                         encodeLineStringCoordinates(geom))

  private def encodePolygonCoordinates[P <: Position](geom: Polygon[P]): Json =
    Json.arr(geom.components().toVector.map(ring => ring.getPositions.asJson): _*)

  implicit def encodePolygon[P <: Position](implicit crsE: Encoder[CrsId]): Encoder[Polygon[P]] =
    (geom: Polygon[P]) =>
      assembleSimpleGeom(geom.getGeometryType,
                         geom.getCoordinateReferenceSystem.getCrsId.asJson,
                         encodePolygonCoordinates(geom))

  implicit def encodeMultiPoint[P <: Position](
      implicit crsE: Encoder[CrsId]): Encoder[MultiPoint[P]] =
    (geom: MultiPoint[P]) =>
      assembleSimpleGeom(geom.getGeometryType,
                         geom.getCoordinateReferenceSystem.getCrsId.asJson,
                         geom.getPositions.asJson)

  implicit def encodeMultiLineString[P <: Position](
      implicit crsE: Encoder[CrsId]): Encoder[MultiLineString[P]] =
    (geom: MultiLineString[P]) =>
      assembleSimpleGeom(geom.getGeometryType,
                         geom.getCoordinateReferenceSystem.getCrsId.asJson,
                         Json.arr(geom.components().map(encodeLineStringCoordinates): _*))

  implicit def encodeMultiPolygon[P <: Position](
      implicit crsE: Encoder[CrsId]): Encoder[MultiPolygon[P]] =
    (geom: MultiPolygon[P]) =>
      assembleSimpleGeom(geom.getGeometryType,
                         geom.getCoordinateReferenceSystem.getCrsId.asJson,
                         Json.arr(
                           geom
                             .components()
                             .map(encodePolygonCoordinates): _*
                         ))

  implicit def encodeGeomColl[P <: Position](
      implicit crsE: Encoder[CrsId]): Encoder[GeometryCollection[P]] =
    (geom: GeometryCollection[P]) =>
      Json.obj(
        "type" -> Json.fromString("GeometryCollection"),
        "crs"  -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
        "geometries" -> {
          implicit val crsEncoder: Encoder[CrsId] = Encoder.instance(_ => Json.Null)
          implicit val crsE: Encoder[Geometry[P]] = encodeGeometry
          Json.arr(geom.components().map(g => g.asJson): _*)
        }
    )

  implicit def encodeGeometry[P <: Position](implicit crsE: Encoder[CrsId]): Encoder[Geometry[P]] =
    Encoder.instance {
      case g: Point[P]               => g.asJson(encodePoint)
      case l: LineString[P]          => l.asJson(encodeLineString)
      case p: Polygon[P]             => p.asJson(encodePolygon)
      case mp: MultiPoint[P]         => mp.asJson(encodeMultiPoint)
      case ml: MultiLineString[P]    => ml.asJson(encodeMultiLineString)
      case mpl: MultiPolygon[P]      => mpl.asJson(encodeMultiPolygon)
      case gc: GeometryCollection[P] => gc.asJson(encodeGeomColl)
      case _                         => sys.error("no encoder")
    }

  implicit val geomTypeDecoder: Decoder[GeometryType] =
    Decoder.decodeString.emapTry(s => Try(GeometryType.valueOf(s.toUpperCase)))

  implicit val pointDecoder: Decoder[PointHolder] = Decoder.decodeArray[Double].map(PointHolder)
  implicit val pointListDecoder: Decoder[PointListHolder] =
    Decoder.decodeArray[PointHolder].map(PointListHolder)
  implicit val PointListListDecoder: Decoder[PointListListHolder] =
    Decoder.decodeArray[PointListHolder].map(PointListListHolder)
  implicit val PolygonListDecoder: Decoder[PolygonListHolder] =
    Decoder.decodeArray[PointListListHolder].map(PolygonListHolder)

  def geometryDecoder(defaultCrs: CoordinateReferenceSystem[_])(
      implicit crsD: Decoder[CrsId]): Decoder[Geometry[_]] =
    (c: HCursor) => {
      for {
        crsId  <- c.downField("crs").as[Option[CrsId]]
        typeId <- c.downField("type").as[GeometryType]
        crs = crsId
          .map(CrsRegistry.getCoordinateReferenceSystem(_, defaultCrs))
          .getOrElse(defaultCrs)
        geom <- if (isSimple(typeId)) SimpleGeometryDecoder(crs, typeId)(c)
        else c.as[Geometry[_]](GeometryCollectionDecoder(crs))
      } yield geom

    }

  def isSimple(gtype: GeometryType): Boolean =
    gtype != GEOMETRYCOLLECTION

  def GeometryCollectionDecoder(crs: CoordinateReferenceSystem[_])(
      implicit crsD: Decoder[CrsId]): Decoder[Geometry[_]] =
    (c: HCursor) => {
      val gf = c.downField("geometries")
      for {
        parts <- gf.as[List[Geometry[_]]](Decoder.decodeList(geometryDecoder(crs)))
        geom = if (parts.isEmpty) Geometries.mkEmptyGeometryCollection(crs)
        else Geometries.mkGeometryCollection(parts: _*)
      } yield geom
    }

  def SimpleGeometryDecoder(crs: CoordinateReferenceSystem[_],
                            gtype: GeometryType): Decoder[Geometry[_]] =
    (c: HCursor) => {
      val cf = c.downField("coordinates")
      for {
        coordinates <- gtype match {
          case POINT           => cf.as[PointHolder]
          case LINESTRING      => cf.as[PointListHolder]
          case POLYGON         => cf.as[PointListListHolder]
          case MULTIPOINT      => cf.as[PointListHolder]
          case MULTILINESTRING => cf.as[PointListListHolder]
          case MULTIPOLYGON    => cf.as[PolygonListHolder]
          case _               => throw new IllegalArgumentException(s"Can't deserialize type $gtype")
        }
        adjusted = adjustTo(crs, coordinates.getCoordinateDimension)
        builder  = GeometryBuilder(coordinates, gtype)
      } yield {
        builder.build(adjusted)
      }
    }

  case class GeometryBuilder(holder: Holder, gType: GeometryType) {
    def build[P <: Position](crs: CoordinateReferenceSystem[P]): Geometry[P] =
      holder.toGeometry(crs, gType)
  }

  sealed trait Holder {
    def getCoordinateDimension: Int
    def toGeometry[P <: Position](crs: CoordinateReferenceSystem[P],
                                  gtype: GeometryType): Geometry[P]
  }

  case class PointHolder(dbls: Array[Double]) extends Holder {
    def getCoordinateDimension: Int = dbls.length
    override def toGeometry[P <: Position](crs: CoordinateReferenceSystem[P],
                                           gtype: GeometryType): Geometry[P] = {
      if (dbls.length == 0) return Geometries.mkEmptyPoint(crs)
      val p = Positions.getFactoryFor(crs.getPositionClass).mkPosition(dbls: _*)
      Geometries.mkPoint(p, crs)
    }
  }

  case class PointListHolder(points: Array[PointHolder]) extends Holder {
    override def getCoordinateDimension: Int =
      points.foldLeft(0)((dim, ph) => Math.max(dim, ph.getCoordinateDimension))
    override def toGeometry[P <: Position](crs: CoordinateReferenceSystem[P],
                                           gtype: GeometryType): Geometry[P] = {
      if (points.length == 0) return Geometries.mkEmptyLineString(crs)
      val pf  = Positions.getFactoryFor(crs.getPositionClass)
      val psb = PositionSequenceBuilders.fixedSized(points.length, crs.getPositionClass)
      points.foreach(ph => psb.add(pf.mkPosition(ph.dbls: _*)))
      val ps = psb.toPositionSequence
      gtype match {
        case LINESTRING => Geometries.mkLineString(ps, crs)
        case LINEARRING => Geometries.mkLinearRing(ps, crs)
        case MULTIPOINT => Geometries.mkMultiPoint(ps, crs)
        case _          => throw new IllegalArgumentException(s"Can't deserialize type $gtype")
      }
    }
  }

  case class PointListListHolder(ptLists: Array[PointListHolder]) extends Holder {
    override def getCoordinateDimension: Int =
      ptLists.foldLeft(0)((dim, ph) => Math.max(dim, ph.getCoordinateDimension))
    override def toGeometry[P <: Position](crs: CoordinateReferenceSystem[P],
                                           gtype: GeometryType): Geometry[P] = {
      if (ptLists.length == 0) return Geometries.mkEmptyGeometry(gtype, crs)
      gtype match {
        case MULTILINESTRING =>
          Geometries.mkMultiLineString(
            ptLists.map(ptl => ptl.toGeometry(crs, LINESTRING).asInstanceOf[LineString[P]]): _*)
        case POLYGON =>
          Geometries.mkPolygon(
            ptLists.map(ptl => ptl.toGeometry(crs, LINEARRING).asInstanceOf[LinearRing[P]]): _*)
        case _ => throw new IllegalArgumentException(s"Can't deserialize type $gtype")
      }
    }
  }

  case class PolygonListHolder(ptlLists: Array[PointListListHolder]) extends Holder {
    override def getCoordinateDimension: Int =
      ptlLists.foldLeft(0)((dim, ph) => Math.max(dim, ph.getCoordinateDimension))
    override def toGeometry[P <: Position](crs: CoordinateReferenceSystem[P],
                                           gtype: GeometryType): Geometry[P] = {
      if (ptlLists.length == 0) return Geometries.mkEmptyMultiPolygon(crs)
      Geometries.mkMultiPolygon(
        ptlLists.map(ptl => ptl.toGeometry(crs, POLYGON).asInstanceOf[Polygon[P]]): _*)
    }
  }
}

object GeoJsonCodec extends CrsIdCodec with GeometryCodec {
  implicit val crsE: Encoder[CrsId]                           = crsEncoderByName
  implicit val crsD: Decoder[CrsId]                           = crsDecoderByName
  implicit val gDecoder: Decoder[Geometry[_]] = geometryDecoder(WGS84)
}
