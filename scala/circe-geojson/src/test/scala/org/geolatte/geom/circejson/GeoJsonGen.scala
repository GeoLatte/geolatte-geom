package org.geolatte.geom.circejson

import org.geolatte.geom.syntax.GeometryImplicits._
import org.geolatte.geom._
import com.fasterxml.jackson.databind.ObjectMapper
import org.geolatte.geom.crs.CoordinateReferenceSystem
import org.geolatte.geom.generator.GeometryGenerators
import org.geolatte.geom.json.GeolatteGeomModule
import org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84
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
      val pb: PositionBuilder[T, P]) {

    private val box   = envelope(crs)(lowerLeft, upperRight)
    val pointGen      = GeometryGenerators.point(box)
    val lineGen       = GeometryGenerators.lineString(5, box)
    val polyGen       = GeometryGenerators.polygon(6, box)
    val multiPointGen = GeometryGenerators.multiPoint(4, box)
    val multiLineGen  = GeometryGenerators.multiLineString(4, 5, box)
    val multiPolyGen  = GeometryGenerators.multiPolygon(4, 6, box)
    val geometryCollectionGen = GeometryGenerators.geometryCollection(4,
                                                                      pointGen,
                                                                      lineGen,
                                                                      polyGen,
                                                                      multiPointGen,
                                                                      multiLineGen,
                                                                      multiPolyGen)
  }

}
