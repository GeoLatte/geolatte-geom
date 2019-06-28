import org.geolatte.geom.{C2D, C3D}
import org.geolatte.geom.crs.CoordinateReferenceSystems._
import org.geolatte.geom.types.Point
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
class GeometryTypesTest extends FlatSpec {

  import org.geolatte.geom.syntax.GeometryImplicits._
  import org.geolatte.geom.types._

  "A 3D Cartesian Point" should "be a assignable to a 2D Cartesian Point" in {

    val pZ: Point[C3D] = point(PROJECTED_3D_METER)(2.0, 3.0, 100.0)

    val p: Point[C2D] = pZ

    p shouldBe a[Point[_]]

    assert( p.getPosition.getX == 2.0 && p.getPosition.getY == 3.0)


  }

  "A 3D Cartesian Geometry" should "be a assignable to a 2D Cartesian Geometry" in {

    val pZ: Geometry[C3D] = point(PROJECTED_3D_METER)(2.0, 3.0, 100.0)

    val p: Geometry[C2D] = pZ

    p shouldBe a[Geometry[_]]

    assert( p.getPositions.getPositionN(0).getX == 2.0 && p.getPositionN(0).getY == 3.0)


  }


}
