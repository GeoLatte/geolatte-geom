package org.geolatte.geom.circejson

import com.fasterxml.jackson.databind.ObjectMapper
import org.geolatte.geom._
import org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84
import org.geolatte.geom.crs.{CoordinateReferenceSystem, CoordinateReferenceSystems, LinearUnit}
import org.geolatte.geom.generator.GeometryGenerators
import org.geolatte.geom.json.{GeolatteGeomModule, Setting}
import org.geolatte.geom.syntax.{GeometryImplicits, PositionBuilder}
import org.scalacheck.Gen

object GeoJsonGen {

  import org.geolatte.geom.syntax.GeometryImplicits._

  def buildMapper(crs: CoordinateReferenceSystem[_], settings: (Setting, Boolean)*) : ObjectMapper = {
    val om = new ObjectMapper()
    val module =new GeolatteGeomModule(crs)
    settings.foreach{ case (s, v) => module.set(s,v)}
    om.registerModule(module)
    om
  }

  private val mapper = buildMapper(WGS84)

  private val noCrsMapper = buildMapper(WGS84, Setting.SUPPRESS_CRS_SERIALIZATION->true)

  implicit class JsonStringable[P <: Position](geom: Geometry[P]) {
    def asFasterXMLJsonString(withCrs: Boolean = true): String = if (withCrs) mapper.writeValueAsString(geom) else
      noCrsMapper.writeValueAsString(geom)
  }

  /**
    * Created by Karel Maesen, Geovise BVBA on 24/09/2018.
    */
  case class GeneratorSet[T, P <: Position](lowerLeft: T, upperRight: T)(
      implicit val crs: CoordinateReferenceSystem[P],
      val pb: PositionBuilder[T, P]
  ) {

    private val bbox   = box(crs)(lowerLeft, upperRight)
    val pointGen      = GeometryGenerators.point(bbox)
    val lineGen       = GeometryGenerators.lineString(5, bbox)
    val polyGen       = GeometryGenerators.polygon(6, bbox)
    val multiPointGen = GeometryGenerators.multiPoint(4, bbox)
    val multiLineGen  = GeometryGenerators.multiLineString(4, 5, bbox)
    val multiPolyGen  = GeometryGenerators.multiPolygon(4, 6, bbox)
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

  implicit val crs2DGen: Gen[CoordinateReferenceSystem[G2D]] =
    Gen.const(CoordinateReferenceSystems.WGS84)

  implicit val crs3DGen: Gen[CoordinateReferenceSystem[G3D]] =
    Gen.const(CoordinateReferenceSystems.WGS84.addVertical() )

  implicit def generator2DSetGen[P <: Position](
      implicit crs: CoordinateReferenceSystem[P],
      pb: PositionBuilder[(Double, Double), P]
  ): Gen[GeneratorSet[(Double, Double), P]] = {
    for {
      lowerLeft  <- Gen.choose(0d, 40d)
      upperRight <- Gen.choose(50d, 80d)
    } yield GeneratorSet((lowerLeft, lowerLeft), (upperRight, upperRight))
  }

  implicit def generator3DSetGen[P <: Position](
      implicit crs: CoordinateReferenceSystem[P],
      pb: PositionBuilder[(Double, Double, Double), P]
  ): Gen[GeneratorSet[(Double, Double, Double), P]] = {
    for {
      lowerLeft  <- Gen.choose(0d, 40d)
      upperRight <- Gen.choose(50d, 80d)
    } yield
      GeneratorSet(
        (lowerLeft, lowerLeft, lowerLeft),
        (upperRight, upperRight, upperRight)
      )
  }

  implicit val point2DGen: Gen[Point[G2D]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToG2D)
    point        <- Gen.lzy(generatorSet.pointGen.generate())
  } yield point

  implicit val point3DGen: Gen[Point[G3D]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToG3D)
    point        <- Gen.lzy(generatorSet.pointGen.generate())
  } yield point

  implicit val line2DGen: Gen[LineString[G2D]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToG2D)
    line         <- Gen.lzy(generatorSet.lineGen.generate())
  } yield line

  implicit val line3DGen: Gen[LineString[G3D]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToG3D)
    line         <- Gen.lzy(generatorSet.lineGen.generate())
  } yield line

  implicit val polygon2DGen: Gen[Polygon[G2D]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToG2D)
    polygon      <- Gen.lzy(generatorSet.polyGen.generate())
  } yield polygon

  implicit val polygon3DGen: Gen[Polygon[G3D]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToG3D)
    polygon      <- Gen.lzy(generatorSet.polyGen.generate())
  } yield polygon

  implicit val multiPoint2DGen: Gen[MultiPoint[G2D]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToG2D)
    multiPoint   <- Gen.lzy(generatorSet.multiPointGen.generate())
  } yield multiPoint

  implicit val multiPoint3DGen: Gen[MultiPoint[G3D]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToG3D)
    multiPoint   <- Gen.lzy(generatorSet.multiPointGen.generate())
  } yield multiPoint

  implicit val multiLine2DGen: Gen[MultiLineString[G2D]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToG2D)
    multiLine    <- Gen.lzy(generatorSet.multiLineGen.generate())
  } yield multiLine

  implicit val multiLine3DGen: Gen[MultiLineString[G3D]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToG3D)
    multiLine    <- Gen.lzy(generatorSet.multiLineGen.generate())
  } yield multiLine

  implicit val multiPolygon2DGen: Gen[MultiPolygon[G2D]] = for {
    crs          <- crs2DGen
    generatorSet <- generator2DSetGen(crs, GeometryImplicits.tupleToG2D)
    multiPolygon <- Gen.lzy(generatorSet.multiPolyGen.generate())
  } yield multiPolygon

  implicit val multiPolygon3DGen: Gen[MultiPolygon[G3D]] = for {
    crs          <- crs3DGen
    generatorSet <- generator3DSetGen(crs, GeometryImplicits.tupleToG3D)
    multiPolygon <- Gen.lzy(generatorSet.multiPolyGen.generate())
  } yield multiPolygon

  implicit val geometry2DGen: Gen[Geometry[G2D]] = Gen.oneOf(
    point2DGen,
    line2DGen,
    polygon2DGen,
    multiPoint2DGen,
    multiLine2DGen,
    multiPolygon2DGen
  )

  implicit val geometry3DGen: Gen[Geometry[G3D]] = Gen.oneOf(
    point3DGen,
    line3DGen,
    polygon3DGen,
    multiPoint3DGen,
    multiLine3DGen,
    multiPolygon3DGen
  )

  implicit val geometryCollection2DGen: Gen[GeometryCollection[G2D]] = for {
    aantal     <- Gen.choose(1, 10)
    geometries <- Gen.listOfN(aantal, geometry2DGen)
  } yield new GeometryCollection[G2D](geometries: _*)

  implicit val geometryCollection3DGen: Gen[GeometryCollection[G3D]] = for {
    aantal     <- Gen.choose(1, 10)
    geometries <- Gen.listOfN(aantal, geometry3DGen)
  } yield new GeometryCollection[G3D](geometries: _*)

}
