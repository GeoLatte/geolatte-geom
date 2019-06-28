package org.geolatte.geom

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
package object types {

  import org.geolatte.geom

  //Re-export Position types
  type Position = geom.Position
  type C2D = geom.C2D
  type C3D = geom.C3D
  type C2DM = geom.C2DM
  type C3DM = geom.C3DM
  type G2D = geom.G2D
  type G3D = geom.G3D
  type G2DM = geom.G2DM
  type G3DM = geom.G3DM


  //covariant re-definition of types
  type Feature[+P,ID] = geom.Feature[Position,ID]
  type Geometry[+P <: Position]   = geom.Geometry[_ <: P]

  type Point[+P <: Position] = geom.Point[ _ <: P]
  type LinearRing[+P <: Position] = geom.LinearRing[ _ <: P]
  type LineString[+P <: Position] = geom.LineString[ _ <: P]

  type Polygon[+P <: Position] = geom.Polygon[ _ <: P]
  type MultiPoint[+P <: Position] = geom.MultiPoint[ _ <: P]

  type MultiLineString[+P <: Position] = geom.MultiLineString[ _ <: P]
  type MultiPolygon[+P <: Position] = geom.MultiPolygon[ _ <: P]

  type GeometryCollection[P <: Position] = geom.GeometryCollection[ _ <: P, Geometry[P]]

}
