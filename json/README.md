
[![javadoc](https://javadoc.io/badge2/org.geolatte/geolatte-geojson/javadoc.svg)](https://javadoc.io/doc/org.geolatte/geolatte-geojson)


# GeoJson module

This modules provides a Jackson module to (de)serialize `Geometry`, `Feature` and `FeatureCollection` instances to 
(from) GeoJson.

# How to use
Add the following dependency to the POM.

```xml

<dependency>
    <groupId>org.geolatte</groupId>
    <artifactId>geolatte-geojson</artifactId>
    <version>${geolatte-geom-version}</version>
</dependency>
```

The library provides a custom module `GeolatteGeomModule` that can be added to Jackson `ObjectMapper`.

```java
    ObjectMapper mapper=new ObjectMapper();
    mapper.registerModule(new GeolatteGeomModule());
```

# Configuration

Optionally you can customize the `GeolatteGeomModule` behavior by:

- setting the default `CoordinateReferenceSystem` to use
- setting a feature flag

## Configuring a default Coordinate Reference System

You can configure a default Coordinate Reference System by passing it in the constructor of the `GeolatteGeomModule`

```java
    CoordinateReferenceSystem<G2D> crs=...;
    ObjectMapper mapper=new ObjectMapper();
    mapper.registerModule(new GeolatteGeomModule(crs));
```

If no default is specified in the constructor, the default will be set to `WGS84`.
The default Coordinate Reference System will be used during deserialization from GeoJson whenever one of the following
conditions hold:

- the GeoJson `Geometry` doesn't have a `crs` element
- the `IGNORE_CRS` feature flag setting is set (see below)


## Feature flags

You can set a feature flag after creation of the `GeolatteGeomModule`:

```java
    GeolatteGeomModule module = new GeolatteGeomModule();
    module.set(Setting.IGNORE_CRS, true);
    mapper = new ObjectMapper();
    mapper.registerModule(module);
```

The following settings are currently supported:
- `IGNORE_CRS`: ignore the`crs` element in the GeoJson `Geometry` (if any) and always use the default coordinate (default: `false`) 
- `SUPPRESS_CRS_SERIALIZATION`: do not serialize a `crs` object in `Geometry` GeoJsons (default: `false`)
- `SERIALIZE_CRS_AS_URN`: serialize `crs` as a URN (default: `false`)


