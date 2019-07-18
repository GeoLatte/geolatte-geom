//The package name can't be scala because of problems in intellij import resolution (scala package classes not found)
package org.geolatte.geom.syntax

import org.geolatte.{geom => jgeom}
import jgeom.Position
import org.geolatte.geom.builder.DSL
import org.geolatte.geom.crs.CoordinateReferenceSystems.mkCoordinateReferenceSystem
import org.geolatte.geom.crs.Unit.METER
import org.geolatte.geom.crs._
import org.geolatte.geom._

trait PositionBuilder[-T, +P] {
  def apply(t: T): P
}

trait PositionSeqBuilder[-T, +P] {
  def apply(t: Seq[T]): Seq[P]
}

sealed trait PositionType {

  import jgeom._
  def getPositionType(pclass: Class[_]): PositionType =
    if (classOf[G2D].isAssignableFrom(pclass)) Geodetic else Cartesian

  def unapply(pcl: Class[_]): Boolean

  def getPositionType(crs: CoordinateReferenceSystem[_]): PositionType =
    getPositionType(crs.getPositionClass)

  def unapply(crs: CoordinateReferenceSystem[_]): Boolean = unapply(crs.getPositionClass)
}

case object Geodetic extends PositionType {
  def unapply(pClass: Class[_]): Boolean = getPositionType(pClass) == Geodetic
}

case object Cartesian extends PositionType {
  def unapply(pClass: Class[_]): Boolean = getPositionType(pClass) == Cartesian
}

trait PositionBuilders {

  def positionBuilder[T, P <: Position](crs: CoordinateReferenceSystem[P])(
      implicit pb: PositionBuilder[T, P]): PositionBuilder[T, P] = pb

  import jgeom.{C2D, C3D, C2DM, C3DM, G2D, G2DM, G3D, G3DM}
  implicit val c2D: Class[C2D]   = classOf[C2D]
  implicit val c3D: Class[C3D]   = classOf[C3D]
  implicit val c2DM: Class[C2DM] = classOf[C2DM]
  implicit val c3DM: Class[C3DM] = classOf[C3DM]
  implicit val g2D: Class[G2D]   = classOf[G2D]
  implicit val g3D: Class[G3D]   = classOf[G3D]
  implicit val g2DM: Class[G2DM] = classOf[G2DM]
  implicit val g3DM: Class[G3DM] = classOf[G3DM]

  implicit val tupleToC2D: PositionBuilder[(Double, Double), C2D] = PositionBuilderWrapper(
    (DSL.c: (Double, Double) => C2D).tupled.apply _
  )

  implicit val tupleToC2DM: PositionBuilder[(Double, Double, Double), C2DM] =
    PositionBuilderWrapper(
      (DSL.cM: (Double, Double, Double) => C2DM).tupled.apply _
    )

  implicit val tupleToC3D: PositionBuilder[(Double, Double, Double), C3D] = PositionBuilderWrapper(
    (DSL.c: (Double, Double, Double) => C3D).tupled.apply _
  )

  implicit val tupleToC3DM: PositionBuilder[(Double, Double, Double, Double), C3DM] =
    PositionBuilderWrapper(
      (DSL.c: (Double, Double, Double, Double) => C3DM).tupled.apply _
    )

  implicit val tupleToG2D: PositionBuilder[(Double, Double), G2D] = PositionBuilderWrapper(
    (DSL.g: (Double, Double) => G2D).tupled.apply _
  )

  implicit val tupleToG2DM: PositionBuilder[(Double, Double, Double), G2DM] =
    PositionBuilderWrapper(
      (DSL.gM: (Double, Double, Double) => G2DM).tupled.apply _
    )

  implicit val tupleToG3D: PositionBuilder[(Double, Double, Double), G3D] = PositionBuilderWrapper(
    (DSL.g: (Double, Double, Double) => G3D).tupled.apply _
  )

  implicit val tupleToG3DM: PositionBuilder[(Double, Double, Double, Double), G3DM] =
    PositionBuilderWrapper(
      (DSL.g: (Double, Double, Double, Double) => G3DM).tupled.apply _
    )

  case class PositionBuilderWrapper[-T, +P](builder: T => P) extends PositionBuilder[T, P] {
    def apply(t: T) = builder(t)
  }

  implicit def positionSeqBuilder[T, P](
      implicit pb: PositionBuilder[T, P]): PositionSeqBuilder[T, P] =
    new PositionSeqBuilder[T, P] {
      def apply(tuples: Seq[T]): Seq[P] = tuples.map(pb(_))
    }

}

trait GeometryConstructors {

  import org.geolatte.geom.builder.DSL

  def envelope[P <: Position, T](crs: CoordinateReferenceSystem[P])(lowerLeft: T, upperRight: T)(
      implicit pb: PositionBuilder[T, P]): Envelope[P] =
    new org.geolatte.geom.Envelope(pb.apply(lowerLeft), pb.apply(upperRight), crs)

  def envelope[P <: Position, T](lowerLeft: T, upperRight: T)(
      implicit crs: CoordinateReferenceSystem[P],
      pb: PositionBuilder[T, P]): Envelope[P] =
    envelope(crs)(lowerLeft, upperRight)(pb)

  def point[P <: Position, T](crs: CoordinateReferenceSystem[P])(tuple: T)(
      implicit pb: PositionBuilder[T, P]): Point[P] =
    point(tuple)(crs, pb)

  def point[P <: Position, T](tuple: T)(implicit crs: CoordinateReferenceSystem[P],
                                        pb: PositionBuilder[T, P]): Point[P] =
    DSL.point(crs, pb.apply(tuple))

  def lineString[P <: Position, T](crs: CoordinateReferenceSystem[P])(tuples: T*)(
      implicit pb: PositionSeqBuilder[T, P]): LineString[P] =
    lineString(tuples: _*)(crs, pb)

  def lineString[P <: Position, T](tuples: T*)(implicit crs: CoordinateReferenceSystem[P],
                                               pb: PositionSeqBuilder[T, P]): LineString[P] =
    DSL.linestring(crs, pb.apply(tuples): _*)

  def linearRing[P <: Position, T](crs: CoordinateReferenceSystem[P])(tuples: T*)(
      implicit pb: PositionSeqBuilder[T, P]): LinearRing[P] =
    linearRing(tuples: _*)(crs, pb)

  def linearRing[P <: Position, T](tuples: T*)(implicit crs: CoordinateReferenceSystem[P],
                                               pb: PositionSeqBuilder[T, P]): LinearRing[P] =
    DSL.ring(crs, pb.apply(tuples): _*)

  def polygon[P <: Position](rings: LinearRing[P]*)(
      implicit crs: CoordinateReferenceSystem[P]): Polygon[P] =
    if (rings.isEmpty) {
      new jgeom.Polygon(crs)
    } else {
      val (h, t) = castback[LinearRing[P], jgeom.LinearRing[P]](rings.head, rings.tail)
      DSL.polygon(h, t: _*)
    }

  def multiLinestring[P <: Position](lines: LineString[P]*)(
      implicit crs: CoordinateReferenceSystem[P]): MultiLineString[P] =
    if (lines.isEmpty) {
      new jgeom.MultiLineString(crs)
    } else {
      val (h, t) = castback[LineString[P], jgeom.LineString[P]](lines.head, lines.tail)
      DSL.multilinestring(h, t: _*)
    }

  def multiPoint[P <: Position](points: Point[P]*)(
      implicit crs: CoordinateReferenceSystem[P]): MultiPoint[P] =
    if (points.isEmpty) {
      new jgeom.MultiPoint(crs)
    } else {
      val (h, t) = castback[Point[P], jgeom.Point[P]](points.head, points.tail)
      DSL.multipoint(h, t: _*)
    }

  def multiPolygon[P <: Position](polygons: Polygon[P]*)(
      implicit crs: CoordinateReferenceSystem[P]): MultiPolygon[P] =
    if (polygons.isEmpty) {
      new jgeom.MultiPolygon(crs)
    } else {
      val (h, t) = castback[Polygon[P], jgeom.Polygon[P]](polygons.head, polygons.tail)
      DSL.multipolygon(h, t: _*)
    }

  def geometrycollection[P <: Position](geometries: Geometry[P]*)(
      implicit crs: CoordinateReferenceSystem[P]): GeometryCollection[P, Geometry[P]] =
    if (geometries.isEmpty) {
      new jgeom.GeometryCollection[P, jgeom.Geometry[P]](crs)
    } else {
      val (h, t) = castback[Geometry[P], jgeom.Geometry[P]](geometries.head, geometries.tail)
      DSL.geometrycollection(h, t: _*)
    }

  private[this] def cb[I, O](in: I): O            = in.asInstanceOf[O]
  private[this] def cbb[I, O](in: Seq[I]): Seq[O] = in.asInstanceOf[Seq[O]]

  private[this] def castback[I, O](head: I, tail: Seq[I]): (O, Seq[O]) =
    (cb(head), cb(tail))

}

trait GeometryOps {

  def castTo[P <: Position](implicit ev: Class[P]): Geometry[P]

  def castToCrs[P <: Position](implicit ev: CoordinateReferenceSystem[P]): Geometry[P]

}

trait GeometryOpsImplicit {

  implicit def geometryOpsWrapper[P <: Position](inner: Geometry[P]): GeometryOps =
    new GeometryOps {

      override def castTo[Q <: Position](implicit ev: Class[Q]): Geometry[Q] = {
        inner.as(ev)
      }

      override def castToCrs[Q <: Position](
          implicit ev: CoordinateReferenceSystem[Q]): Geometry[Q] = {
        inner.as(ev.getPositionClass)
      }

    }

}

trait ArrayToPosition {

  case class PFactory[Q <: Position](crs: CoordinateReferenceSystem[Q], make: Array[Double] => Q)

  import org.geolatte.geom.Positions._

  private[this] def adjustCrs[Q <: Position](
      crs: CoordinateReferenceSystem[_],
      coordinateDimension: Int): CoordinateReferenceSystem[Q] = {
    val adjusted = if (coordinateDimension == 3) {
      val extId = crs.getCrsId.extend(METER, null)
      CrsRegistry
        .computeIfAbsent(extId, (key: CrsId) => mkCoordinateReferenceSystem(crs, METER, null))
    } else if (coordinateDimension == 4) {
      val extId = crs.getCrsId.extend(METER, METER)
      CrsRegistry.computeIfAbsent(extId,
                                  (key: CrsId) => mkCoordinateReferenceSystem(crs, METER, METER))
    } else {
      crs
    }

    adjusted.asInstanceOf[CoordinateReferenceSystem[Q]]
  }

  def selectFactory[Q <: Position](crs: CoordinateReferenceSystem[_],
                                   coordinateDimension: Int): PFactory[Q] = {
    val dimDiff = coordinateDimension - crs.getCoordinateDimension
    if (dimDiff <= 0) {
      val adjusted = crs.asInstanceOf[CoordinateReferenceSystem[Q]]
      PFactory[Q](adjusted, (co: Array[Double]) => mkPosition(adjusted, co: _*))
    } else {
      val expandedCrs = adjustCrs[Q](crs, coordinateDimension)
      PFactory[Q](expandedCrs, (co: Array[Double]) => mkPosition(expandedCrs, co: _*))
    }
  }

}

trait ExtendDim[B <: Position, Z <: Position, M <: Position] extends PositionBuilders {
  def baseClass (implicit ev : Class[B]) : Class[B] = ev
  def zClass (implicit ev: Class[Z]) : Class[Z] = ev
  def mClass (implicit ev: Class[M]) : Class[M] = ev
}

object ExtendDim {

  import jgeom.{C2D, C3D, C2DM, C3DM, G2D, G2DM, G3D, G3DM}

  implicit val eDg2D  = new ExtendDim[G2D, G3D, G2DM]   {}
  implicit val eDg3D  = new ExtendDim[G3D, G3D, G3DM]   {}
  implicit val eDg2DM = new ExtendDim[G2DM, G3DM, G2DM] {}
  implicit val eDg3DM = new ExtendDim[G3DM, G3DM, G3DM] {}

  implicit val eDc2D  = new ExtendDim[C2D, C3D, C2DM]   {}
  implicit val eDc3D  = new ExtendDim[C3D, C3D, C3DM]   {}
  implicit val eDc2DM = new ExtendDim[G2DM, C3DM, C2DM] {}
  implicit val eDc3DM = new ExtendDim[C3DM, C3DM, C3DM] {}

}

trait CoordinateSystemExtender {

  import ExtendDim._

  def addVertical[B <: Position, BZ <: Position](
      crs: CoordinateReferenceSystem[B],
      unit: LinearUnit = org.geolatte.geom.crs.Unit.METER)(
      implicit w: ExtendDim[B, BZ, _]): CoordinateReferenceSystem[BZ] =
    CoordinateReferenceSystems
      .addVerticalSystem(crs, unit)
      .asInstanceOf[org.geolatte.geom.crs.CoordinateReferenceSystem[BZ]]

  def addMeasure[B <: Position, BM <: Position](
      crs: CoordinateReferenceSystem[B],
      unit: LinearUnit = org.geolatte.geom.crs.Unit.METER)(
      implicit w: ExtendDim[B, _, BM]): CoordinateReferenceSystem[BM] =
    CoordinateReferenceSystems
      .addLinearSystem(crs, unit)
      .asInstanceOf[org.geolatte.geom.crs.CoordinateReferenceSystem[BM]]

}

object CoordinateReferenceSystemSyntax extends CoordinateSystemExtender {

  self =>

  implicit class CoordinateReferenceWrapper[B <: Position](crs: CoordinateReferenceSystem[B]) {
    def addVertical[BZ <: Position](unit: LinearUnit = org.geolatte.geom.crs.Unit.METER)(
        implicit w: ExtendDim[B, BZ, _]): CoordinateReferenceSystem[BZ] =
      self.addVertical(crs, unit)

    def addMeasure[BM <: Position](unit: LinearUnit = org.geolatte.geom.crs.Unit.METER)(
        implicit w: ExtendDim[B, _, BM]): CoordinateReferenceSystem[BM] = self.addMeasure(crs, unit)

  }

}

trait ContraVariantOps extends GeometryOpsImplicit with PositionBuilders {
  import ExtendDim._

  case class ClassRepr[B <: Position, BZ <: B, BM <: B, G[_] <: Geometry[B]]
  (gc : Class[G[B]])
  (implicit ed: ExtendDim[B, BZ, BM], cr : Class[B]) {
    type OUTPUT = G[B]
    def cast(g: G[_]) : OUTPUT = g.castTo[B](cr).asInstanceOf[OUTPUT]
  }

  implicit def pClass[B <: Position, BZ <: B, BM <: B](implicit ed: ExtendDim[B, BZ, BM], cr: Class[B]) = ClassRepr(classOf[Point[B]])(ed, cr)


//  implicit def geomZToBase[B <: Position, BZ <: B, G <: Geometry[BZ]](point: G)(
//    implicit
//    ed: ExtendDim[B, BZ, _],
//    b: Class[B],
//    g: Class[G]) : g.type =
//      point.as(ed.baseClass)

//  implicit def pointZToBase[B <: Position, BZ <: Position](point: Point[BZ])(implicit ed: ExtendDim[B, BZ, _], b: Class[B]) : Point[B] =
//    point.as(ed.baseClass)
//  implicit def pointMToBase[B <: Position, BM <: Position](point: Point[BM])(implicit ed: ExtendDim[B, _, BM], b: Class[B]) : Point[B] =
//    point.as(ed.baseClass)

//  implicit def lsZToBase[B <: Position, BZ <: Position](ls: LineString[BZ])(implicit ed: ExtendDim[B, BZ, _], b: Class[B]) : LineString[B] =
//    ls.as(ed.baseClass)
//  implicit def lsMToBase[B <: Position, BM <: Position](ls: LineString[BM])(implicit ed: ExtendDim[B, _, BM], b: Class[B]) : LineString[B] =
//    ls.as(ed.baseClass)

//  implicit def geomToBase[B <: Position, BZ <: Position](geom: Geometry[BZ])(implicit ed: ExtendDim[B, BZ, _], b: Class[B]) : Geometry[B] =
//    geom.as(ed.baseClass)



  }

object GeometryImplicits
    extends PositionBuilders
    with GeometryConstructors
    with GeometryOpsImplicit
    with ContraVariantOps
