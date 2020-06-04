package org.geolatte.geom.circe

import io.circe._
import io.circe.syntax._
import org.geolatte.geom.GeometryType._
import org.geolatte.geom.circe.GeoJsonCodec.{
  GeometryBuilder,
  PointHolder,
  PointListHolder,
  PointListListHolder,
  PolygonListHolder
}
import org.geolatte.geom.{PositionSequence, _}
import org.geolatte.geom.crs.{
  CoordinateReferenceSystem,
  CoordinateReferenceSystems,
  CrsId,
  CrsRegistry
}
import org.geolatte.geom.crs.CoordinateReferenceSystems._

import scala.util.Try

object GeoJsonCodec {

  import scala.jdk.CollectionConverters._

  implicit def encodePosition[P <: Position]: Encoder[P] = new Encoder[P] {
    override def apply(a: P): Json = {
      val ar = new Array[Double](a.getCoordinateDimension)
      a.toArray(ar)
      ar.asJson
    }
  }

  implicit def encodePositions[P <: Position]: Encoder[PositionSequence[P]] =
    new Encoder[PositionSequence[P]] {
      override def apply(poss: PositionSequence[P]): Json = {
        val css = poss.asScala.map(p => p.asInstanceOf[Position].asJson).toSeq
        Json.arr(css: _*)
      }
    }

  implicit val encodeCrs: Encoder[CrsId] = new Encoder[CrsId] {
    override def apply(crs: CrsId): Json =
      Json.obj(
        "type"       -> Json.fromString("name"),
        "properties" -> Json.obj("name" -> Json.fromString(crs.toString))
      )
  }

  //TODO -- is there a better way to do this?
  private[this] def dropCrs(js: Json): Json = return js.hcursor.downField("crs").delete.focus.get

  implicit def encodePoint[P <: Position]: Encoder[Point[P]] = new Encoder[Point[P]] {
    override def apply(geom: Point[P]): Json =
      Json.obj(
        "type"        -> Json.fromString("Point"),
        "crs"         -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
        "coordinates" -> geom.getPosition.asJson
      )
  }

  private def encodeLineStringCoordinates[P <: Position](geom: LineString[P]): Json =
    geom.getPositions.asJson

  implicit def encodeLineString[P <: Position]: Encoder[LineString[P]] =
    new Encoder[LineString[P]] {
      override def apply(geom: LineString[P]): Json =
        Json.obj(
          "type"        -> Json.fromString("LineString"),
          "crs"         -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
          "coordinates" -> encodeLineStringCoordinates(geom)
        )
    }

  private def encodePolygonCoordinates[P <: Position](geom: Polygon[P]): Json =
    Json.arr(geom.components().toVector.map(ring => ring.getPositions.asJson): _*)

  implicit def encodePolygon[P <: Position]: Encoder[Polygon[P]] = new Encoder[Polygon[P]] {
    override def apply(geom: Polygon[P]): Json =
      Json.obj(
        "type"        -> Json.fromString("Polygon"),
        "crs"         -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
        "coordinates" -> encodePolygonCoordinates(geom)
      )
  }

  implicit def encodeMultiPoint[P <: Position]: Encoder[MultiPoint[P]] =
    new Encoder[MultiPoint[P]] {
      override def apply(geom: MultiPoint[P]): Json =
        Json.obj(
          "type"        -> Json.fromString("MultiPoint"),
          "crs"         -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
          "coordinates" -> geom.getPositions.asJson
        )
    }

  implicit def encodeMultiLineString[P <: Position]: Encoder[MultiLineString[P]] =
    new Encoder[MultiLineString[P]] {
      override def apply(geom: MultiLineString[P]): Json =
        Json.obj(
          "type"        -> Json.fromString("MultiLineString"),
          "crs"         -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
          "coordinates" -> Json.arr(geom.components().map(encodeLineStringCoordinates): _*)
        )
    }

  implicit def encodeMultiPolygon[P <: Position]: Encoder[MultiPolygon[P]] =
    new Encoder[MultiPolygon[P]] {
      override def apply(geom: MultiPolygon[P]): Json =
        Json.obj(
          "type" -> Json.fromString("MultiPolygon"),
          "crs"  -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
          "coordinates" -> Json.arr(
            geom
              .components()
              .map(encodePolygonCoordinates): _*
          )
        )
    }

  implicit def encodeGeomColl[P <: Position]: Encoder[GeometryCollection[P]] =
    new Encoder[GeometryCollection[P]] {
      override def apply(geom: GeometryCollection[P]): Json =
        Json.obj(
          "type"       -> Json.fromString("GeometryCollection"),
          "crs"        -> geom.getCoordinateReferenceSystem.getCrsId.asJson,
          "geometries" -> Json.arr(geom.components().map(g => dropCrs(g.asJson)): _*)
        )
    }

  implicit def encodeGeometry[P <: Position]: Encoder[Geometry[P]] = Encoder.instance {
    case g: Point[P]               => encodePoint(g)
    case l: LineString[P]          => encodeLineString(l)
    case p: Polygon[P]             => encodePolygon(p)
    case mp: MultiPoint[P]         => encodeMultiPoint(mp)
    case ml: MultiLineString[P]    => encodeMultiLineString(ml)
    case mpl: MultiPolygon[P]      => encodeMultiPolygon(mpl)
    case gc: GeometryCollection[P] => encodeGeomColl(gc)
    case _                         => sys.error("no encoder")
  }

  val epsgDecoder: Decoder[CrsId] = Decoder.decodeString.emapTry { str =>
    Try(CrsId.parse(str))
  }

  implicit val crsidDecoder: Decoder[CrsId] = new Decoder[CrsId] {
    override def apply(c: HCursor): Decoder.Result[CrsId] =
      for {
        crsId <- c.downField("properties").downField("name").as[CrsId](epsgDecoder)
      } yield crsId
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

  def geometryDecoder(defaultCrs: CoordinateReferenceSystem[_]): Decoder[Geometry[_]] = new Decoder[Geometry[_]] {
    override def apply(c: HCursor): Decoder.Result[Geometry[_ <: Position]] = {
      for {
        crsId  <- c.downField("crs").as[Option[CrsId]]
        typeId <- c.downField("type").as[GeometryType]
        crs = crsId.map(CrsRegistry.getCoordinateReferenceSystem(_, defaultCrs)).getOrElse(defaultCrs)
        geom <- if(isSimple(typeId)) SimpleGeometryDecoder(crs, typeId)(c) else GeometryCollectionDecoder(crs, typeId)(c)
      } yield geom

    }
  }

  implicit val gDecoder: Decoder[Geometry[_]] = geometryDecoder(WGS84)


  def isSimple(gtype: GeometryType): Boolean =
    gtype != GEOMETRYCOLLECTION

  def GeometryCollectionDecoder(crs: CoordinateReferenceSystem[_],
                                 gtype: GeometryType): Decoder[Geometry[_]] = new Decoder[Geometry[_]] {
    override def apply(c: HCursor): Decoder.Result[Geometry[_]] = {
      val gf = c.downField("geometries")
      for {
        parts <- gf.as[List[Geometry[_]]](Decoder.decodeList(geometryDecoder(crs)))
        geom = Geometries.mkGeometryCollection(parts:_*)
      } yield geom
    }
  }

  def SimpleGeometryDecoder(crs: CoordinateReferenceSystem[_],
                            gtype: GeometryType): Decoder[Geometry[_]] =
    new Decoder[Geometry[_]] {
      override def apply(c: HCursor): Decoder.Result[Geometry[_]] = {
        val cf = c.downField("coordinates")
        for {
          coordinates <- gtype match {
            case POINT           => cf.as[PointHolder]
            case LINESTRING      => cf.as[PointListHolder]
            case POLYGON         => cf.as[PointListListHolder]
            case MULTIPOINT      => cf.as[PointListHolder]
            case MULTILINESTRING => cf.as[PointListListHolder]
            case MULTIPOLYGON    => cf.as[PolygonListHolder]
          }
          adjusted = adjustTo(crs, coordinates.getCoordinateDimension)
          builder  = GeometryBuilder(coordinates, gtype)
        } yield {
          builder.build(adjusted)
        }
      }
    }

  case class GeometryBuilder(holder: Holder, gType: GeometryType) {
    def build[P <: Position](crs: CoordinateReferenceSystem[P]) = holder.toGeometry(crs, gType)
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
