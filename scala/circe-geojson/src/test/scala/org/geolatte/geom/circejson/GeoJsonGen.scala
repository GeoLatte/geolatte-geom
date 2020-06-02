package org.geolatte.geom.circejson

import com.fasterxml.jackson.databind.ObjectMapper
import org.geolatte.geom._
import org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84
import org.geolatte.geom.crs.{CoordinateReferenceSystem, CoordinateReferenceSystems}
import org.geolatte.geom.generator.GeometryGenerators
import org.geolatte.geom.json.GeolatteGeomModule
import org.geolatte.geom.syntax.{GeometryImplicits, PositionBuilder}
import org.scalacheck.Gen

object GeoJsonGen {

  import org.geolatte.geom.syntax.GeometryImplicits._

  private val mapper = {
    val om = new ObjectMapper()
    om.registerModule(new GeolatteGeomModule(WGS84))
    om
  }

  implicit class JsonStringable[P <: Position](geom: Geometry[P]) {
    def asJsonString: String = mapper.writeValueAsString(geom)
  }

  /**
    * Created by Karel Maesen, Geovise BVBA on 24/09/2018.
    */
  case class GeneratorSet[T, P <: Position](lowerLeft: T, upperRight: T)(
      implicit val crs: CoordinateReferenceSystem[P],
      val pb: PositionBuilder[T, P]
  ) {

    private val box   = envelope(crs)(lowerLeft, upperRight)
    val pointGen      = GeometryGenerators.point(box)
    val lineGen       = GeometryGenerators.lineString(5, box)
    val polyGen       = GeometryGenerators.polygon(6, box)
    val multiPointGen = GeometryGenerators.multiPoint(4, box)
    val multiLineGen  = GeometryGenerators.multiLineString(4, 5, box)
    val multiPolyGen  = GeometryGenerators.multiPolygon(4, 6, box)
    val geometryCollectionGen = GeometryGenerators.geometryCollection(
      4,
      pointGen,
      lineGen,
      polyGen,
      multiPointGen,
      multiLineGen,
      multiPolyGen
    )
  }

  implicit val crs2DGen: Gen[CoordinateReferenceSystem[C2DM]] =
    Gen.const(CoordinateReferenceSystems.PROJECTED_2DM_METER)

  implicit val crs3DGen: Gen[CoordinateReferenceSystem[C3DM]] =
    Gen.const(CoordinateReferenceSystems.PROJECTED_3DM_METER)

  implicit def generator2DSetGen[P <: Position](
      implicit crs: CoordinateReferenceSystem[P],
      pb: PositionBuilder[(Double, Double, Double), P]
  ): Gen[GeneratorSet[(Double, Double, Double), P]] = {
    for {
      lowerLeft  <- Gen.choose(0d, 20d)
      upperRight <- Gen.choose(190d, 210d)
      metered    <- Gen.choose(0d, 1d)
    } yield GeneratorSet((lowerLeft, lowerLeft, metered), (upperRight, upperRight, metered))
  }

  implicit def generator3DSetGen[P <: Position](
      implicit crs: CoordinateReferenceSystem[P],
      pb: PositionBuilder[(Double, Double, Double, Double), P]
  ): Gen[GeneratorSet[(Double, Double, Double, Double), P]] = {
    for {
      lowerLeft  <- Gen.choose(0d, 20d)
      upperRight <- Gen.choose(190d, 210d)
      metered    <- Gen.choose(0d, 1d)
    } yield
      GeneratorSet(
        (lowerLeft, lowerLeft, lowerLeft, metered),
        (upperRight, upperRight, upperRight, metered)
      )
  }

  implicit val point2DGen: Gen[Point[C2DM]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToC2DM)
    point        <- Gen.lzy(generatorSet.pointGen.generate())
  } yield point

  implicit val point3DGen: Gen[Point[C3DM]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToC3DM)
    point        <- Gen.lzy(generatorSet.pointGen.generate())
  } yield point

  implicit val line2DGen: Gen[LineString[C2DM]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToC2DM)
    line         <- Gen.lzy(generatorSet.lineGen.generate())
  } yield line

  implicit val line3DGen: Gen[LineString[C3DM]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToC3DM)
    line         <- Gen.lzy(generatorSet.lineGen.generate())
  } yield line

  implicit val polygon2DGen: Gen[Polygon[C2DM]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToC2DM)
    polygon      <- Gen.lzy(generatorSet.polyGen.generate())
  } yield polygon

  implicit val polygon3DGen: Gen[Polygon[C3DM]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToC3DM)
    polygon      <- Gen.lzy(generatorSet.polyGen.generate())
  } yield polygon

  implicit val multiPoint2DGen: Gen[MultiPoint[C2DM]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToC2DM)
    multiPoint   <- Gen.lzy(generatorSet.multiPointGen.generate())
  } yield multiPoint

  implicit val multiPoint3DGen: Gen[MultiPoint[C3DM]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToC3DM)
    multiPoint   <- Gen.lzy(generatorSet.multiPointGen.generate())
  } yield multiPoint

  implicit val multiLine2DGen: Gen[MultiLineString[C2DM]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToC2DM)
    multiLine    <- Gen.lzy(generatorSet.multiLineGen.generate())
  } yield multiLine

  implicit val multiLine3DGen: Gen[MultiLineString[C3DM]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToC3DM)
    multiLine    <- Gen.lzy(generatorSet.multiLineGen.generate())
  } yield multiLine

  implicit val multiPolygon2DGen: Gen[MultiPolygon[C2DM]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToC2DM)
    multiPolygon <- Gen.lzy(generatorSet.multiPolyGen.generate())
  } yield multiPolygon

  implicit val multiPolygon3DGen: Gen[MultiPolygon[C3DM]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToC3DM)
    multiPolygon <- Gen.lzy(generatorSet.multiPolyGen.generate())
  } yield multiPolygon

  implicit val geometry2DGen: Gen[Geometry[C2DM]] = Gen.oneOf(
    point2DGen,
    line2DGen,
    polygon2DGen,
    multiPoint2DGen,
    multiLine2DGen,
    multiPolygon2DGen
  )

  implicit val geometry3DGen: Gen[Geometry[C3DM]] = Gen.oneOf(
    point3DGen,
    line3DGen,
    polygon3DGen,
    multiPoint3DGen,
    multiLine3DGen,
    multiPolygon3DGen
  )

  implicit val geometryCollection2DGen: Gen[GeometryCollection[C2DM]] = for {
    aantal     <- Gen.choose(1, 10)
    geometries <- Gen.listOfN(aantal, geometry2DGen)
  } yield new GeometryCollection[C2DM](geometries: _*)

  implicit val geometryCollection3DGen: Gen[GeometryCollection[C3DM]] = for {
    aantal     <- Gen.choose(1, 10)
    geometries <- Gen.listOfN(aantal, geometry3DGen)
  } yield new GeometryCollection[C3DM](geometries: _*)

}
