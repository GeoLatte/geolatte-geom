
[![javadoc](https://javadoc.io/badge2/org.geolatte/geolatte-geojson-jackson3/javadoc.svg)](https://javadoc.io/doc/org.geolatte/geolatte-geojson-jackson3)


# GeoJson module — Jackson 3 adapter

This module provides a Jackson 3 adapter that registers a `GeolatteGeomModule` for
(de)serializing `Geometry`, `Feature` and `FeatureCollection` instances to/from GeoJSON.

The actual GeoJSON encoding/decoding logic lives in the Jackson-free
`geolatte-geojson-core` module; this module is a thin Jackson 3 adapter on top of it.
A separate `geolatte-geojson-jackson2` module provides the equivalent for Jackson 2.x.

> **Upgrading from 1.x:**
> - The Maven coordinate changed from `org.geolatte:geolatte-geojson` to
>   `org.geolatte:geolatte-geojson-jackson3`.
> - The adapter classes (`GelatteGeomModule`, the per-type
>   serializers/deserializers, etc.) moved from `org.geolatte.geom.json` to
>   `org.geolatte.geom.json.jackson3`. Update your imports accordingly.
> - The data types (`Setting`, `GeoJsonFeature`, `GeoJsonFeatureCollection`)
>   stay in `org.geolatte.geom.json` and now live in the
>   `geolatte-geojson-core` artifact.

# How to use
Add the following dependency to the POM.

```xml

<dependency>
    <groupId>org.geolatte</groupId>
    <artifactId>geolatte-geojson-jackson3</artifactId>
    <version>${geolatte-geom-version}</version>
</dependency>
```

This will pull in the Jackson 3 `jackson-databind` dependency. Note that the Jackson dependency
is declared as `<optional>true</optional>`: consumers must declare the desired Jackson 3 version
explicitly in their own POM.

The library provides a custom module `GeolatteGeomModule` (in package
`org.geolatte.geom.json.jackson3`) that can be added to a Jackson 3 `ObjectMapper`.

```java
    import org.geolatte.geom.json.jackson3.GeolatteGeomModule;

    ObjectMapper mapper = JsonMapper.builder()
        .addModule(new GeolatteGeomModule())
        .build();
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


