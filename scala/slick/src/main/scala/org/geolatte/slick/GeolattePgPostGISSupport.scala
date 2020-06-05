package org.geolatte.slick

import java.sql.{PreparedStatement, ResultSet}

import com.github.tminglei.slickpg.geom.PgPostGISExtensions
import com.github.tminglei.slickpg.{ExPostgresProfile, utils}
import org.geolatte.geom._
import org.geolatte.geom.codec._
import slick.ast.FieldSymbol
import slick.jdbc.{PostgresProfile, _}
import slick.lifted.Rep

import scala.language.implicitConversions
import scala.reflect.{ClassTag, classTag}

trait GeolattePgPostGISSupport extends PgPostGISExtensions { driver: PostgresProfile =>

  type GEOMETRY           = Geometry[_]
  type GEOGRAPHY          = Geometry[_]
  type POINT              = Point[_]
  type LINESTRING         = LineString[_]
  type POLYGON            = Polygon[_]
  type GEOMETRYCOLLECTION = GeometryCollection[_]

  trait PostGISCodeGenSupport {
    // register types to let `ExModelBuilder` find them
    //noinspection TypeCheckCanBeMatch
    if (driver.isInstanceOf[ExPostgresProfile]) {
      driver.asInstanceOf[ExPostgresProfile].bindPgTypeToScala("geometry", classTag[Geometry[_]])
    }
  }

  ///
  trait PostGISAssistants
      extends BasePostGISAssistants[Geometry[_], Geometry[_], Point[_], LineString[_], Polygon[_], GeometryCollection[
        _
      ]]

  //noinspection TypeAnnotation
  trait PostGISImplicits extends PostGISCodeGenSupport {
    implicit val geometryTypeMapper: JdbcType[Geometry[_]] = new GeometryJdbcType[Geometry[_]]
    implicit val geometryTypeMapperC2D: JdbcType[Geometry[C2D]] =
      new GeometryJdbcType[Geometry[C2D]]
    implicit val geometryTypeMapperG2D: JdbcType[Geometry[G2D]] =
      new GeometryJdbcType[Geometry[G2D]]
    implicit val geometryTypeMapperC3D: JdbcType[Geometry[C3D]] =
      new GeometryJdbcType[Geometry[C3D]]
    implicit val geometryTypeMapperG3D: JdbcType[Geometry[G3D]] =
      new GeometryJdbcType[Geometry[G3D]]

    implicit val pointTypeMapper: JdbcType[Point[_]]      = new GeometryJdbcType[Point[_]]
    implicit val pointTypeMapperC2D: JdbcType[Point[C2D]] = new GeometryJdbcType[Point[C2D]]
    implicit val pointTypeMapperG2D: JdbcType[Point[G2D]] = new GeometryJdbcType[Point[G2D]]
    implicit val pointTypeMapperC3D: JdbcType[Point[C3D]] = new GeometryJdbcType[Point[C3D]]
    implicit val pointTypeMapperG3D: JdbcType[Point[G3D]] = new GeometryJdbcType[Point[G3D]]

    implicit val polygonTypeMapper: JdbcType[Polygon[_]]      = new GeometryJdbcType[Polygon[_]]
    implicit val polygonTypeMapperC2D: JdbcType[Polygon[C2D]] = new GeometryJdbcType[Polygon[C2D]]
    implicit val polygonTypeMapperG2D: JdbcType[Polygon[G2D]] = new GeometryJdbcType[Polygon[G2D]]
    implicit val polygonTypeMapperC3D: JdbcType[Polygon[C3D]] = new GeometryJdbcType[Polygon[C3D]]
    implicit val polygonTypeMapperG3D: JdbcType[Polygon[G3D]] = new GeometryJdbcType[Polygon[G3D]]

    implicit val lineStringTypeMapper: JdbcType[LineString[_]] = new GeometryJdbcType[LineString[_]]
    implicit val lineStringTypeMapperC2D: JdbcType[LineString[C2D]] =
      new GeometryJdbcType[LineString[C2D]]
    implicit val lineStringTypeMapperG2D: JdbcType[LineString[G2D]] =
      new GeometryJdbcType[LineString[G2D]]
    implicit val lineStringTypeMapperC3D: JdbcType[LineString[C3D]] =
      new GeometryJdbcType[LineString[C3D]]
    implicit val lineStringTypeMapperG3D: JdbcType[LineString[G3D]] =
      new GeometryJdbcType[LineString[G3D]]

    implicit val linearRingTypeMapper: JdbcType[LinearRing[_]] = new GeometryJdbcType[LinearRing[_]]
    implicit val linearRingTypeMapperC2D: JdbcType[LinearRing[C2D]] =
      new GeometryJdbcType[LinearRing[C2D]]
    implicit val linearRingTypeMapperG2D: JdbcType[LinearRing[G2D]] =
      new GeometryJdbcType[LinearRing[G2D]]
    implicit val linearRingTypeMapperC3D: JdbcType[LinearRing[C3D]] =
      new GeometryJdbcType[LinearRing[C3D]]
    implicit val linearRingTypeMapperG3D: JdbcType[LinearRing[G3D]] =
      new GeometryJdbcType[LinearRing[G3D]]

    implicit val geometryCollectionTypeMapper: JdbcType[GeometryCollection[_]] =
      new GeometryJdbcType[GeometryCollection[_]]
    implicit val geometryCollectionTypeMapperC2D: JdbcType[GeometryCollection[C2D]] =
      new GeometryJdbcType[GeometryCollection[C2D]]
    implicit val geometryCollectionTypeMapperG2D: JdbcType[GeometryCollection[G2D]] =
      new GeometryJdbcType[GeometryCollection[G2D]]
    implicit val geometryCollectionTypeMapperC3D: JdbcType[GeometryCollection[C3D]] =
      new GeometryJdbcType[GeometryCollection[C3D]]
    implicit val geometryCollectionTypeMapperG3D: JdbcType[GeometryCollection[G3D]] =
      new GeometryJdbcType[GeometryCollection[G3D]]

    implicit val multiPointTypeMapper: JdbcType[MultiPoint[_]] = new GeometryJdbcType[MultiPoint[_]]
    implicit val multiPointTypeMapperC2D: JdbcType[MultiPoint[C2D]] =
      new GeometryJdbcType[MultiPoint[C2D]]
    implicit val multiPointTypeMapperG2D: JdbcType[MultiPoint[G2D]] =
      new GeometryJdbcType[MultiPoint[G2D]]
    implicit val multiPointTypeMapperC3D: JdbcType[MultiPoint[C3D]] =
      new GeometryJdbcType[MultiPoint[C3D]]
    implicit val multiPointTypeMapperG3D: JdbcType[MultiPoint[G3D]] =
      new GeometryJdbcType[MultiPoint[G3D]]

    implicit val multiPolygonTypeMapper: JdbcType[MultiPolygon[_]] =
      new GeometryJdbcType[MultiPolygon[_]]
    implicit val multiPolygonTypeMapperC2D: JdbcType[MultiPolygon[C2D]] =
      new GeometryJdbcType[MultiPolygon[C2D]]
    implicit val multiPolygonTypeMapperG2D: JdbcType[MultiPolygon[G2D]] =
      new GeometryJdbcType[MultiPolygon[G2D]]
    implicit val multiPolygonTypeMapperC3D: JdbcType[MultiPolygon[C3D]] =
      new GeometryJdbcType[MultiPolygon[C3D]]
    implicit val multiPolygonTypeMapperG3D: JdbcType[MultiPolygon[G3D]] =
      new GeometryJdbcType[MultiPolygon[G3D]]

    implicit val multiLineStringTypeMapper: JdbcType[MultiLineString[_]] =
      new GeometryJdbcType[MultiLineString[_]]
    implicit val multiLineStringTypeMapperC2D: JdbcType[MultiLineString[C2D]] =
      new GeometryJdbcType[MultiLineString[C2D]]
    implicit val multiLineStringTypeMapperG2D: JdbcType[MultiLineString[G2D]] =
      new GeometryJdbcType[MultiLineString[G2D]]
    implicit val multiLineStringTypeMapperC3D: JdbcType[MultiLineString[C3D]] =
      new GeometryJdbcType[MultiLineString[C3D]]
    implicit val multiLineStringTypeMapperG3D: JdbcType[MultiLineString[G3D]] =
      new GeometryJdbcType[MultiLineString[G3D]]

    ///
    implicit def geometryColumnExtensionMethods[G1 <: Geometry[_]](c: Rep[G1]) =
      new GeometryColumnExtensionMethods[Geometry[_], Point[_], LineString[_], Polygon[_], GeometryCollection[
        _
      ], G1, G1](c)
    implicit def geometryOptionColumnExtensionMethods[G1 <: Geometry[_]](c: Rep[Option[G1]]) =
      new GeometryColumnExtensionMethods[Geometry[_], Point[_], LineString[_], Polygon[_], GeometryCollection[
        _
      ], G1, Option[G1]](c)
  }

  //noinspection TypeAnnotation
  trait PostGISPlainImplicits extends PostGISCodeGenSupport {
    import GeolattePgPostGISSupport._
    import utils.PlainSQLUtils._

    implicit class PostGISPositionedResult(r: PositionedResult) {
      def nextGeometry[T <: Geometry[_]](): T = nextGeometryOption().getOrElse(null.asInstanceOf[T])
      def nextGeometryOption[T <: Geometry[_]](): Option[T] =
        r.nextStringOption().map(fromLiteral[T])
    }

    ////////////////////////////////////////////////////////////////////////////////
    implicit val getGeometry       = mkGetResult(_.nextGeometry[Geometry[_]]())
    implicit val getGeometryOption = mkGetResult(_.nextGeometryOption[Geometry[_]]())

    implicit object SetGeometry extends SetParameter[Geometry[_]] {
      def apply(v: Geometry[_], pp: PositionedParameters): Unit = setGeometry(Option(v), pp)
    }
    implicit object SetGeometryOption extends SetParameter[Option[Geometry[_]]] {
      def apply(v: Option[Geometry[_]], pp: PositionedParameters): Unit = setGeometry(v, pp)
    }

    ///
    private def setGeometry[T <: Geometry[_]](v: Option[T], p: PositionedParameters): Unit =
      v match {
        case Some(v) => p.setBytes(toBytes(v))
        case None    => p.setNull(java.sql.Types.OTHER)
      }
  }

  ////// geometry jdbc type
  class GeometryJdbcType[T <: Geometry[_]](implicit override val classTag: ClassTag[T])
      extends DriverJdbcType[T] {
    import GeolattePgPostGISSupport._

    override def sqlType: Int = java.sql.Types.OTHER

    override def sqlTypeName(sym: Option[FieldSymbol]): String = "geometry"

    override def getValue(r: ResultSet, idx: Int): T = {
      val value = r.getString(idx)
      if (r.wasNull) null.asInstanceOf[T] else fromLiteral[T](value)
    }

    override def setValue(v: T, p: PreparedStatement, idx: Int): Unit = p.setBytes(idx, toBytes(v))

    override def updateValue(v: T, r: ResultSet, idx: Int): Unit = r.updateBytes(idx, toBytes(v))

    override def hasLiteralForm: Boolean = false

    override def valueToSQLLiteral(v: T): String = if (v eq null) "NULL" else s"'${toLiteral(v)}'"
  }
}

object GeolattePgPostGISSupport {

  //////
  private val wktWriterHolder = new ThreadLocal[WktEncoder] {
    override def initialValue(): WktEncoder = Wkt.newEncoder(Wkt.Dialect.POSTGIS_EWKT_1)
  }
  private val wktReaderHolder = new ThreadLocal[WktDecoder] {
    override def initialValue(): WktDecoder = Wkt.newDecoder(Wkt.Dialect.POSTGIS_EWKT_1)
  }
  private val wkbWriterHolder = new ThreadLocal[WkbEncoder] {
    override def initialValue(): WkbEncoder = Wkb.newEncoder(Wkb.Dialect.POSTGIS_EWKB_1)
  }
  private val wkbReaderHolder = new ThreadLocal[WkbDecoder] {
    override def initialValue(): WkbDecoder = Wkb.newDecoder(Wkb.Dialect.POSTGIS_EWKB_1)
  }

  private def toLiteral(geom: Geometry[_]): String = {
    wktWriterHolder.get.encode(geom)
  }

  private def fromLiteral[T](value: String): T = {
    if (value.startsWith("00") || value.startsWith("01")) {
      fromBytes(ByteBuffer.from(value))
    } else {
      wktReaderHolder.get.decode(value).asInstanceOf[T]
    }
  }

  private def toBytes(geom: Geometry[_]): Array[Byte] = {
    wkbWriterHolder.get.encode(geom, ByteOrder.NDR).toByteArray
  }

  private def fromBytes[T](bytes: ByteBuffer): T = {
    wkbReaderHolder.get.decode(bytes).asInstanceOf[T]
  }

}
