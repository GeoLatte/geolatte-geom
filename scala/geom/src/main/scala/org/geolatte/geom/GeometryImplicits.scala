//The package name can't be scala because of problems in intellij import resolution (scala package classes not found)
package org.geolatte.geom

import org.geolatte.geom.builder.DSL
import org.geolatte.geom.crs.CoordinateReferenceSystem

trait PositionBuilder[-T, +P] {
  def apply(t: T) : P
}

trait PositionSeqBuilder[-T, +P]  {
  def apply(t: Seq[T]) : Seq[P]
}



trait PositionBuilders {
  case class PositionBuilderWrapper[-T, +P](builder: T => P) extends PositionBuilder[T, P]{
    def apply(t: T) = builder(t)
  }


  implicit val c2D : Class[C2D] = classOf[C2D]
  implicit val c3D : Class[C3D] = classOf[C3D]
  implicit val c2DM : Class[C2DM] = classOf[C2DM]
  implicit val c3DM : Class[C3DM] = classOf[C3DM]
  implicit val g2D : Class[G2D] = classOf[G2D]
  implicit val g3D : Class[G3D] = classOf[G3D]
  implicit val g2DM : Class[G2DM] = classOf[G2DM]
  implicit val g3DM : Class[G3DM] = classOf[G3DM]

  implicit val tupleToC2D : PositionBuilder[(Double, Double), C2D] = PositionBuilderWrapper(
    (DSL.c : (Double, Double) => C2D).tupled.apply _
  )

  implicit val tupleToC2DM: PositionBuilder[(Double, Double, Double), C2DM] = PositionBuilderWrapper(
    (DSL.cM: (Double, Double, Double) => C2DM).tupled.apply _
  )

  implicit val tupleToC3D : PositionBuilder[(Double, Double, Double), C3D] = PositionBuilderWrapper(
    (DSL.c : (Double, Double,Double) => C3D).tupled.apply _
  )

  implicit val tupleToC3DM : PositionBuilder[(Double, Double, Double, Double), C3DM] = PositionBuilderWrapper(
    (DSL.c : (Double, Double,Double, Double) => C3DM).tupled.apply _
  )

  implicit val tupleToG2D : PositionBuilder[(Double, Double), G2D] = PositionBuilderWrapper(
    (DSL.g : (Double, Double) => G2D).tupled.apply _
  )

  implicit val tupleToG2DM : PositionBuilder[(Double, Double, Double), G2DM] = PositionBuilderWrapper(
    (DSL.gM : (Double, Double, Double) => G2DM).tupled.apply _
  )

  implicit val tupleToG3D : PositionBuilder[(Double, Double, Double), G3D] = PositionBuilderWrapper(
    (DSL.g : (Double, Double,Double) => G3D).tupled.apply _
  )

  implicit val tupleToG3DM : PositionBuilder[(Double, Double, Double, Double), G3DM] = PositionBuilderWrapper(
    (DSL.g : (Double, Double,Double, Double) => G3DM).tupled.apply _
  )


  def positionBuilder[T,P <: Position](crs: CoordinateReferenceSystem[P])(implicit pb: PositionBuilder[T,P]) : PositionBuilder[T,P]
   = pb

  implicit def positionSeqBuilder[T, P](implicit pb: PositionBuilder[T,P]) : PositionSeqBuilder[T,P] =
    new PositionSeqBuilder[T,P] {
      def apply(tuples: Seq[T]) : Seq[P] = tuples.map(pb(_))
    }
  
}


trait GeometryConstructors{

  import org.geolatte.geom.builder.DSL

  def point[P <: Position, T](tuple : T)(implicit crs: CoordinateReferenceSystem[P], pb: PositionBuilder[T,P]) : Point[P] =
    DSL.point( crs, pb.apply( tuple ) )

  def point[P <: Position, T](crs: CoordinateReferenceSystem[P])(tuple : T)(implicit pb: PositionBuilder[T,P]) : Point[P] =
    point(tuple)(crs, pb)

  def lineString[P <: Position, T](tuples : T*)(implicit crs: CoordinateReferenceSystem[P], pb: PositionSeqBuilder[T,P]): LineString[P] =
    DSL.linestring( crs,  pb.apply(tuples):_*)

  def lineString[P <: Position, T](crs: CoordinateReferenceSystem[P])(tuples : T*)(implicit pb: PositionSeqBuilder[T,P]) : LineString[P] =
    lineString(tuples:_*)(crs, pb)

  def linearRing[P <: Position, T](tuples: T*)(implicit crs: CoordinateReferenceSystem[P], pb: PositionSeqBuilder[T,P]) : LinearRing[P] =
    DSL.ring(crs, pb.apply(tuples):_*)

  def linearRing[P <: Position, T](crs: CoordinateReferenceSystem[P])(tuples : T*)(implicit pb: PositionSeqBuilder[T,P]) : LinearRing[P] =
    linearRing(tuples:_*)(crs,pb)

  def polygon[P <: Position](rings: LinearRing[P]*)(implicit crs: CoordinateReferenceSystem[P]) : Polygon[P] =
    if (rings.isEmpty) new Polygon(crs)
    else DSL.polygon(rings.head, rings.tail:_* )

  def multiLinestring[P <: Position](lines: LineString[P]*)(implicit crs: CoordinateReferenceSystem[P])  : MultiLineString[P]  =
    if(lines.isEmpty) new MultiLineString(crs)
    else DSL.multilinestring(lines.head, lines.tail:_*)

  def multiPoint[P <: Position](points: Point[P]*)(implicit crs: CoordinateReferenceSystem[P])  : MultiPoint[P]  =
    if(points.isEmpty) new MultiPoint(crs)
    else DSL.multipoint(points.head, points.tail:_*)

  def multiPolygon[P <: Position](polygons: Polygon[P]*)(implicit crs: CoordinateReferenceSystem[P])  : MultiPolygon[P]  =
    if(polygons.isEmpty) new MultiPolygon(crs)
    else DSL.multipolygon(polygons.head, polygons.tail:_*)

  def geometrycollection[P <: Position](geometries: Geometry[P]*)(implicit crs: CoordinateReferenceSystem[P]) : GeometryCollection[P, Geometry[P]] =
    if(geometries.isEmpty) new GeometryCollection(crs)
    else DSL.geometrycollection(geometries.head, geometries.tail:_*)

}

trait GeometryOps {

  def castTo[P <: Position](implicit ev: Class[P]) : Geometry[P]

  def castToCrs[P <: Position](implicit ev: CoordinateReferenceSystem[P]): Geometry[P]

}

trait GeometryOpsImplicit {

  implicit def geometryOpsWrapper[P <: Position](inner: Geometry[P])  : GeometryOps = new GeometryOps {

    override def castTo[Q <: Position](implicit ev : Class[Q]) : Geometry[Q] = {
      inner.as(ev)
    }

    override def castToCrs[Q <: Position](implicit ev: CoordinateReferenceSystem[Q]): Geometry[Q] = {
    inner.as(ev.getPositionClass)
    }

  }

}


object GeometryImplicits extends PositionBuilders
  with GeometryConstructors
  with GeometryOpsImplicit


