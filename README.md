[![Build Status](http://teamcity.geolatte.org/app/rest/builds/buildType:\(id:bt2\)/statusIcon)](http://teamcity.geolatte.org/viewType.html?buildTypeId=bt2&guest=1)

# Geolatte-geom

A geometry model for Java that conforms to the Simple Features For SQL specification 1.2.1.

It is intended as a drop-in replacement for the Java Topology Suite (JTS) geometry model. GeoLatte-geom is fully
interoperable with JTS but offers the following additional features:
* immutable data structures (Geometries are value objects).
* support for 2D, 3D, 2DM and 3DM geometries
* support for several dialects of WKT/WKB (Postgis, Sql Server, SFS 1.21)
* pluggable, extendable Geometry operations
* CRS-awareness (knowledge of coordinate reference system (projected/geodetic, angular units of metres)
* geodetic operations (length, distance and area calculations)


# Redesign 0.12 => 0.13

This version is a complete redesign. The redesign is aimed at:
* increasing the level of type-safety in the API;
* the ability to incorporate geographic (or geodetic) data without duplication of the Geometry class hierarchy;
* making full use of the information on coordinate reference systems that is now available in the CRSRegistry.

## The Coordinate Reference System model (crs package)

The availability of the CRSRegistry, and the explicit modelling of coordinate reference systems,
 made it obvious that a better way was available for handling the different dimensions of a Geometry's coordinate space.
 In previous versions this dimensionality was specified with a DimensionalFlag. Having a complete model of the
 Coordinate Reference System, including it's coordinate space, means we no longer need this. Rather than
 associating a DimensionalFlag with the Geometry, we only need it's Coordinate Reference System.

 Since in the Coordinate Reference System model Projected and Geographic systems are distinguished, we can use a
 single Geometry class-hierarchy, and let the implementations inspect the associated Coordinate Reference System
 to determine which operations are valid, or which algorithms are selected (e.g. geodetic length rather than length in 2D plane).

## Base and compound Coordinate Reference Systems

The CRSRegistry only provides access to base systems, which are always 2D. Users can add axes (Vertical or Measure) to
a base system, and so create a Compound system. Vertical axes are axes with a Direction of UP (DOWN), measure axes have
direction OTHER or UNKNOWN. Although it is possible to add several Measure axes, many measure operations will only take the first
such axis into account.


## Positions

A Position is essentially a tuple of coordinates associated with a Coordinate Reference System such that the tuple specifies
a position in the system.

In previous versions, Points played the role of Positions. The concept of a Position, distinguished from
 a Point, was introduced to be able to encode the commonality and differences between positions in the different types of
 Coordinate Reference System as a class-hierarchy without having to do this for all Geometry (sub)classes. Designing this
 as a class-hierarchy improves type-safety.

In this new model, a Geometry is conceptually a set of Positions (all associated with the same Coordinate Reference System).
The set is determined by one or more sequences of Positions and a type enum value (GeometryType) that determine how the
sequence(s) determine the Geometry (e.g. for LineString it is by linear interpolation between the consecutive positions).

Technically a Position is modeled as an abstract generic class with a self-reflexive type bound. It's signature is:

    abstract public class Position<P extends Position<P>>

This design was inspired by the design of Enum in the java standard library. As explained by Angelica Langer, it is "is a
generic type that can only be instantiated for its subtypes, and those subtypes will inherit some useful methods, some
of which take subtype specific arguments (or otherwise depend on the subtype)"
(see: http://www.angelikalanger.com/GenericsFAQ/FAQSections/TypeParameters.html#How%20do%20I%20decrypt%20Enum?).

The "subtype specific arguments" in the case of the Position class is the CoordinateReferenceSystem<P> constructor argument.
Coordinate Reference Systems and Positions are mutually dependent. This dependency is expressed by the Position-bounded type
argument in the CoordinateReferenceSystem class.

## De-emphasizing the Simple Features Specification (SFS)

This library started as an attempt to have a JTS-interoperable library that is (like JTS) SFS-compliant, but has a more
modern design and better support for geometries not in the 2-dimensional plane. It is for me no longer obvious what the
advantage of SFS compliance are. With open source, more expressive languages the advantage of standardisation on the API level
are becoming less-and-less obvious. The only advantages to the SFS model (or it's more complicated cousing SQL/MM-Part 3)
that I can see are a familiar Geometry model and a precise specification of topological relations. These advantages are
offset by the disadvantages of a bias to 2D planar coordinate systems, and a (by current tastes)  problematic API design.
Should complex operations really be part of the Geometry interface? What with alternative algorithm implementations?

Because of these misgivings, we will de-emphasize SFS compliance.

## Open issues

The design is not yet finished. Here are some open issues that require further attention:
* Can the Position class-hierarchy be simplified?
* The DSL is nice and simple, but looses the type-safety we introduced with the Position class-hierarchy. In Scala this
is easy to remedy (e.g. by providing implicit conversion between Tuples and Positions). What is the best way in Java?
Introducing p2D(), g2D(), etc. functions and generically-typed PosTokens?
* Is the API for composing Coordinate Reference System sufficiently clear?
* How to handle empty Geometries, and Positions?
* How to handle cases where the Coordinate Reference System is not known?
* Should we not drop all complex operations from the Geometry API, and only make these available as GeometryOperations?
Then we can provide GeometryOperation Factories that are specific for the type of coordinate space (2D, Measured, Geographic vs. Projected).



