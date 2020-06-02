package org.geolatte.geom.circejson

import org.geolatte.geom.{Geometry, Position}
import org.geolatte.geom.generator.Generator
import org.specs2.mutable.Specification
/**
  * Created by Karel Maesen, Geovise BVBA on 02/06/2020.
  */
class GeometryDecodersSpec extends Specification {

  import io.circe._
  import io.circe.syntax._
  import org.geolatte.geom.circe.GeoJsonCodec._

  import GeoJsonGen._

  "Decoding Unit tests 2D for " >> {

  }

  def testGen[P <: Position](generator: Generator[_ <: Geometry[P]])
}
