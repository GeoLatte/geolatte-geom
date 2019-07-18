package org.geolatte.geom.circejson

import org.geolatte.geom.syntax.GeometryImplicits._
import org.geolatte.geom._
import com.fasterxml.jackson.databind.ObjectMapper
import org.geolatte.geom.crs.CoordinateReferenceSystem
import org.geolatte.geom.generator.Generators
import org.geolatte.geom.json.GeolatteGeomModule
import org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84
import org.geolatte.geom.syntax.PositionBuilder



object GeoJsonGen {

  import org.geolatte.geom.syntax.GeometryImplicits._
  import org.geolatte.geom.types._


  private val mapper = {
    val om = new ObjectMapper()
    om.registerModule( new GeolatteGeomModule( WGS84 ) )
    om
  }

  implicit class JsonStringable[P <: Position](geom: Geometry[P]) {
    def asJsonString: String = mapper.writeValueAsString( geom )
  }


  /**
    * Created by Karel Maesen, Geovise BVBA on 24/09/2018.
    */
  case class GeneratorSet[T, P <: Position]
  (lowerLeft: T, upperRight: T)
  (implicit val crs: CoordinateReferenceSystem[P], val pb: PositionBuilder[T, P]) {
    private val box = envelope( crs )( lowerLeft, upperRight )
    val pointGen = Generators.point( box )
    val lineGen = Generators.lineString( 5, box )
    val polyGen = Generators.polygon( 6, box )
    val multiLineGen = Generators.multiLineString( 4, 5, box )
    val multiPolyGen = Generators.multiPolygon( 4, 6, box )
  }

}