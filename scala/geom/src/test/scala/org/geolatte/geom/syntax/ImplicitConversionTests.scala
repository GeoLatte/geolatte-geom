package org.geolatte.geom.syntax

import org.geolatte.geom._
import org.geolatte.geom.crs.CoordinateReferenceSystems._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
class ImplicitConversionTests extends FlatSpec {

  import org.geolatte.geom.syntax.GeometryImplicits._

  "A 3D Cartesian Point" should "be a assignable to a 2D Cartesian Point" in {

    val pZ: Point[C3D] = point(PROJECTED_3D_METER)(2.0, 3.0, 100.0)


    val p: Point[C2D] = pZ

    p shouldBe a[Point[_]]

    assert( p.getPosition.getX == 2.0 && p.getPosition.getY == 3.0)


  }

  "A 3D Cartesian Geometry" should "be a assignable to a 2D Cartesian Geometry" in {

    val pZ: Point[C3D] = point(PROJECTED_3D_METER)(2.0, 3.0, 100.0)

    val p: Geometry[C2D] = pZ

    p shouldBe a[Geometry[_]]

    assert( p.getPositions.getPositionN(0).getX == 2.0 && p.getPositionN(0).getY == 3.0)


  }

  "A 3D Geodetric LineString" should "be a assignable to a 2D Geodetic Geometry" in {


    val lZ: LineString[G3D] = lineString(WGS84.addVertical())((2.0, 3.0, 100.0), (3.0, 5.0, 102.0))

    val l: Geometry[G2D] = lZ

    l shouldBe a[Geometry[_]]

    assert( l.getPositions.getPositionN(0).getLon == 2.0 && l.getPositionN(0).getLat() == 3.0)


  }



  "A 3D geodetic geometry collection" should "be assigned to a 2D geome" in {
    implicit val crs = WGS84.addVertical()
    val  pz = point((1.2, 2.0, 3.0))
    val  ls = lineString((2.0, 3.0, 100.0), (3.0, 5.0, 102.0))
    val gc = geometrycollection(pz, ls)

    val g : GeometryCollection[G2D] = gc

    assert(g.components()(0) == pz && g.components()(1) == ls)

  }


}
