package org.geolatte.geom.circejson

import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.geolatte.geom._
import org.geolatte.geom.crs.CoordinateReferenceSystem
import org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84
import org.geolatte.geom.generator._
import org.geolatte.geom.json.GeolatteGeomModule
import org.geolatte.geom.syntax.PositionBuilder

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

    import scala.collection.JavaConverters._
    val pointGen: Generator[Point[P]] = GeometryGenerators.point(box)
    val lineGen                       = GeometryGenerators.lineString(5, box)
    val polyGen                       = GeometryGenerators.polygon(6, box)
    val multiPointGen                 = GeometryGenerators.multiPoint(4, box)
    val multiLineGen                  = GeometryGenerators.multiLineString(4, 5, box)
    val multiPolyGen                  = GeometryGenerators.multiPolygon(4, 6, box)
    val geometryCollectionGen = GeometryGenerators.geometryCollection(
      4,
      pointGen,
      lineGen,
      polyGen,
      multiPointGen,
      multiLineGen,
      multiPolyGen
    )
    val combinedGenerator: Generator[Geometry[P]] = GeometryGenerators.combine(
      util.Arrays.asList(
        pointGen,
        lineGen,
        polyGen,
        multiPointGen,
        multiLineGen,
        multiPolyGen,
        geometryCollectionGen
      )
    )
    private val box            = envelope(crs)(lowerLeft, upperRight)
    private val valueGenerator = new ValueGeneratorFactory();
    private val id             = valueGenerator.stringGenerator(5, 8)

    private val propertyMapGenerator = new PropertyMapGenerator(
      5,
      valueGenerator.stringGenerator(3, 5),
      Choice.of(
        List(
          valueGenerator.integerGenerator(0, 100),
          valueGenerator.stringGenerator(0, 10),
          valueGenerator.doubleGenerator(-10, 100)
        ).asJava
      )
    )

    def generate(): Geometry[P] = combinedGenerator.generate

    def generateFeature(): Feature[P, String] =
      TestFeature(combinedGenerator.generate(), id.generate(), propertyMapGenerator.generate())

  }

  case class TestFeature[P <: Position](
      geom: Geometry[P],
      id: String,
      properties: java.util.Map[String, Any]
  ) extends Feature[P, String] {
    override def getGeometry: Geometry[P]                  = geom
    override def getId: String                             = id
    override def getProperties: java.util.Map[String, Any] = properties
  }
}
