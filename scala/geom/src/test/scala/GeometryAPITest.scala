import org.geolatte.geom._
import org.geolatte.geom.crs.CoordinateReferenceSystems._
import org.geolatte.geom.crs.{CoordinateReferenceSystem, Unit}
import org.scalatest.FlatSpec

class GeometryAPITest extends FlatSpec {

  import GeometryImplicits._

  "A 2D Cartesian Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[C2D] = PROJECTED_2D_METER
    val p = point( 2.0, 3.0 )
    assertResult( new Point( new C2D( 2.0, 3.0 ), crs ) )( p )
  }

  "A 2D Geodetic Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[G2D] = org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84
    val p1 = point( 2.0, 3.0 )
    assertResult( new Point( new G2D( 2.0, 3.0 ), crs ) )( p1 )

  }

  "A 2DM Cartesian Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[C2DM] = PROJECTED_2DM_METER
    val p = point( 2.0, 3.0, 4.0 )
    assertResult( new Point( new C2DM( 2.0, 3.0, 4.0 ), crs ) )( p )
  }

  "A 2DM Geodetic Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[G2DM] = addLinearSystem( WGS84, classOf[G2DM], Unit.METER )
    val p = point( 2.0, 3.0, 4.0 )
    assertResult( new Point( new G2DM( 2.0, 3.0, 4.0 ), crs ) )( p )
  }


  "A 3D Cartesian Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[C3D] = PROJECTED_3D_METER
    val p = point( 2.0, 3.0, 4.0 )
    assertResult( new Point( new C3D( 2.0, 3.0, 4.0 ), crs ) )( p )
  }

  "A 3D Geodetic Geometry" should "be easily constructed from a tuple of doubles" in {
    implicit val crs: CoordinateReferenceSystem[G3D] = addVerticalSystem(
      WGS84, classOf[G3D], Unit.METER
    )
    val p = point( 2.0, 3.0, 4.0 )
    assertResult( new Point( new G3D( 2.0, 3.0, 4.0 ), crs ) )( p )
  }

  "A 2DM linestring " should "be easily constructed form tuples " in {
    implicit val crs: CoordinateReferenceSystem[G2DM] = addLinearSystem( WGS84, classOf[G2DM], Unit.METER )
    val l = lineString( (2.0, 3.0, 4.0), (20.0, 40.0, 30.0) )
    val positionSequence = PositionSequenceBuilders.fixedSized( 2, classOf[G2DM] )
      .add( 2.0, 3.0, 4.0 )
      .add( 20.0, 40.0, 30 )
      .toPositionSequence
    val expected = new LineString( positionSequence, crs )
    assertResult( expected )( l )
  }

}

class GeometryApi2Test extends FlatSpec {

  import GeometryImplicits._

  "We " should " easily create Point geometries for a given (non-implicit) CRS" in {

    val crs: CoordinateReferenceSystem[G2DM] = addLinearSystem( WGS84, classOf[G2DM], Unit.METER )
    val p : Point[G2DM] = point(crs)((2.0, 3.0, 4.0))
    assertResult( new Point( new G2DM( 2.0, 3.0, 4.0 ), crs ) )( p )
  }

  "We " should " easily create LineString geometries for a given (non-implicit) CRS" in {

    val crs: CoordinateReferenceSystem[G2DM] = addLinearSystem( WGS84, classOf[G2DM], Unit.METER )
    val l : LineString[G2DM] = lineString(crs)( (2.0, 3.0, 4.0), (33.0, 5.0, 8.0))

    val positionSequence = PositionSequenceBuilders.fixedSized( 2, classOf[G2DM] )
      .add( 2.0, 3.0, 4.0 )
      .add( 33.0, 5.0, 8.0 )
      .toPositionSequence
    val expected = new LineString( positionSequence, crs )
    assertResult( expected )( l )
  }

  "We " should " easily create LinearRing geometries for a given (non-implicit) CRS" in {
    val crs: CoordinateReferenceSystem[G2D] = WGS84
    val l : LinearRing[G2D] = linearRing(crs)( (0.0, 0.0), (2.0, 0.0), (0.0,2.0), (0.0, 0.0))
    assert( l !== null)
  }

}


class GeometryApiAsType extends FlatSpec {

  import GeometryImplicits._

  "We" should "be able to cast to a declared Position type" in {
    implicit val crs: CoordinateReferenceSystem[G2DM] = addLinearSystem( WGS84, classOf[G2DM], Unit.METER )
    val l = lineString((1.0, 2.0, 3.0), (2.0, 3.0, 4.0))

    val l2 = l.castTo[G2D]
  }

  "We" should "be able to cast to the Position type of implicitly declared Crs" in {
    val crs: CoordinateReferenceSystem[G2DM] = addLinearSystem( WGS84, classOf[G2DM], Unit.METER )
    val l = lineString(crs)((1.0, 2.0, 3.0), (2.0, 3.0, 4.0))

    implicit val wgs = WGS84
    val l3 = l.castToCrs
  }

  "Dynamic cast gives runtime error" should "actual geometry pType be inconsistent" in {
    val crs: CoordinateReferenceSystem[C2D] = PROJECTED_2D_METER
    val l = lineString(crs)((1.0, 2.0), (2.0, 3.0))

    implicit val wgs = WGS84
    assertThrows[ClassCastException] {l.castToCrs}
  }

}