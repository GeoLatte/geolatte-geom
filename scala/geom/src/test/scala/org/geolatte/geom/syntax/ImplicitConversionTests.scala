package org.geolatte.geom.syntax

import org.geolatte.geom.crs.CoordinateReferenceSystems._
import org.geolatte.geom.crs.{CoordinateReferenceSystem, Unit}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ImplicitConversionTests extends AnyFlatSpec with Matchers {

  import org.geolatte.geom
  import org.geolatte.geom._
  import org.geolatte.geom.syntax.GeometryImplicits._

  "A 2D Cartesian Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[C2D] = PROJECTED_2D_METER
    val p                                            = point(2.0, 3.0)
    assertResult(new geom.Point(new C2D(2.0, 3.0), crs))(p)
  }

  "A 2D Geodetic Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[G2D] =
      org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84
    val p1 = point(2.0, 3.0)
    assertResult(new geom.Point(new G2D(2.0, 3.0), crs))(p1)

  }

  "A 2DM Cartesian Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[C2DM] = PROJECTED_2DM_METER
    val p                                             = point(2.0, 3.0, 4.0)
    assertResult(new geom.Point(new C2DM(2.0, 3.0, 4.0), crs))(p)
  }

  "A 2DM Geodetic Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[G2DM] =
      addLinearSystem(WGS84, classOf[G2DM], Unit.METER)
    val p = point(2.0, 3.0, 4.0)
    assertResult(new geom.Point(new G2DM(2.0, 3.0, 4.0), crs))(p)
  }

  "A 3D Cartesian Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[C3D] = PROJECTED_3D_METER
    val p                                            = point(2.0, 3.0, 4.0)
    assertResult(new geom.Point(new C3D(2.0, 3.0, 4.0), crs))(p)
  }

  "A 3D Geodetic Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[G3D] = addVerticalSystem(
      WGS84,
      classOf[G3D],
      Unit.METER
    )
    val p = point(2.0, 3.0, 4.0)
    assertResult(new geom.Point(new G3D(2.0, 3.0, 4.0), crs))(p)
  }

  "A 2DM linestring " should "be easily constructed form tuples " in {
    implicit val crs: CoordinateReferenceSystem[G2DM] =
      addLinearSystem(WGS84, classOf[G2DM], Unit.METER)
    val l = lineString((2.0, 3.0, 4.0), (20.0, 40.0, 30.0))
    val positionSequence = geom.PositionSequenceBuilders
      .fixedSized(2, classOf[G2DM])
      .add(2.0, 3.0, 4.0)
      .add(20.0, 40.0, 30)
      .toPositionSequence
    val expected = new geom.LineString(positionSequence, crs)
    assertResult(expected)(l)
  }

  "We " should " easily create Point geometries for a given (non-implicit) CRS" in {

    val crs: CoordinateReferenceSystem[G2DM] = addLinearSystem(WGS84, classOf[G2DM], Unit.METER)
    val p: Point[G2DM]                       = point(crs)((2.0, 3.0, 4.0))
    assertResult(new geom.Point(new G2DM(2.0, 3.0, 4.0), crs))(p)
  }

  "We " should " easily create LineString geometries for a given (non-implicit) CRS" in {

    val crs: CoordinateReferenceSystem[G2DM] = addLinearSystem(WGS84, classOf[G2DM], Unit.METER)
    val l: LineString[G2DM]                  = lineString(crs)((2.0, 3.0, 4.0), (33.0, 5.0, 8.0))

    val positionSequence = geom.PositionSequenceBuilders
      .fixedSized(2, classOf[G2DM])
      .add(2.0, 3.0, 4.0)
      .add(33.0, 5.0, 8.0)
      .toPositionSequence
    val expected = new geom.LineString(positionSequence, crs)
    assertResult(expected)(l)
  }

  "We " should " easily create LinearRing geometries for a given (non-implicit) CRS" in {
    val crs: CoordinateReferenceSystem[G2D] = WGS84
    val l: LinearRing[G2D]                  = linearRing(crs)((0.0, 0.0), (2.0, 0.0), (0.0, 2.0), (0.0, 0.0))
    assert(l !== null)
  }

  "A 3D Cartesian Point" should "be a assignable to a 2D Cartesian Point" in {

    val pZ: Point[C3D] = point(PROJECTED_3D_METER)(2.0, 3.0, 100.0)

    val p: Point[C2D] = pZ

    p shouldBe a[Point[_]]

    assert(p.getPosition.getX == 2.0 && p.getPosition.getY == 3.0)

  }

  "A 3D Cartesian Geometry" should "be a assignable to a 2D Cartesian Geometry" in {

    val pZ: Point[C3D] = point(PROJECTED_3D_METER)(2.0, 3.0, 100.0)

    val p: Geometry[C2D] = pZ

    p shouldBe a[Geometry[_]]

    assert(p.getPositions.getPositionN(0).getX == 2.0 && p.getPositionN(0).getY == 3.0)

  }

  "A 3D Geodetric LineString" should "be a assignable to a 2D Geodetic Geometry" in {

    val lZ: LineString[G3D] = lineString(WGS84.addVertical())((2.0, 3.0, 100.0), (3.0, 5.0, 102.0))

    val l: Geometry[G2D] = lZ

    l shouldBe a[Geometry[_]]

    assert(l.getPositions.getPositionN(0).getLon == 2.0 && l.getPositionN(0).getLat == 3.0)

  }

  "A 3D geodetic geometry collection" should "be assigned to a 2D geome" in {
    implicit val crs: CoordinateReferenceSystem[G3D] = WGS84.addVertical()
    val pz                                           = point((1.2, 2.0, 3.0))
    val ls                                           = lineString((2.0, 3.0, 100.0), (3.0, 5.0, 102.0))
    val gc                                           = geometrycollection(pz, ls)

    val g: GeometryCollection[G2D] = gc

    assert(g.components()(0) == pz && g.components()(1) == ls)

  }
}
