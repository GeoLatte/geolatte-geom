# Release process

The repository tracks two version axes in the root `pom.xml`:

| Axis      | Property            | Used by                                            |
|-----------|---------------------|----------------------------------------------------|
| `geom`    | `${revision}`       | parent aggregator + `geolatte-geom`                |
| `geojson` | `${geojson.version}`| `geolatte-geojson-core`, `-jackson3`, `-jackson2`  |

The `geom` axis groups the parent and `geolatte-geom` because the parent is
pom-only and exists primarily to host shared build configuration for the
geom module. The `geojson` axis groups the three GeoJSON modules because
they share the SPI in `geolatte-geojson-core`; bumping any of them in
isolation would create a configuration where the adapters depend on a
core they were not built against.

The `flatten-maven-plugin` is configured in `defaults` mode so that all
`${...}` placeholders are resolved to concrete versions in the deployed
poms (not just the standard CI-friendly `${revision}`/`${sha1}`/`${changelist}`).

Cross-module dependencies in each child pom reference the producer's
property explicitly:

- `json-core` depends on `geolatte-geom` via `${revision}`
- `json-jackson3` and `json-jackson2` depend on `geolatte-geom` via `${revision}`
  and on `geolatte-geojson-core` via `${geojson.version}`

This is what makes the two axes truly independent — bumping
`${geojson.version}` rebuilds and republishes the three GeoJSON modules
without forcing a new geom release.

## Bumping a version

Use `scripts/bump-version.sh`:

```
scripts/bump-version.sh <axis> <new-version>
```

### Bump only the GeoJSON line

```
scripts/bump-version.sh geojson 1.13.0
mvn -pl json-core,json-jackson3,json-jackson2 -am clean install
mvn -pl json-core,json-jackson3,json-jackson2 deploy -P release
```

The geom axis is unchanged. The three GeoJSON modules are republished
together at the new version.

### Bump the geom (and parent) line

```
scripts/bump-version.sh geom 1.13.0
mvn clean install
mvn -pl geom deploy -P release
```

Bumping geom forces a rebuild of the entire reactor (every other module
depends on geom), but does *not* automatically republish the GeoJSON
modules — they keep their own `${geojson.version}`. If you also want to
publish a new GeoJSON release pinned to the new geom, follow up with a
`geojson` bump.

### Bumping both at once

For a lockstep release (e.g. raising everything to 1.13.0):

```
scripts/bump-version.sh geom    1.13.0
scripts/bump-version.sh geojson 1.13.0
mvn clean install
mvn deploy -P release
```

## Constraints

- A child module's `<parent>` reference is fixed to `${revision}`. If you
  decouple the GeoJSON line from the parent's version (which is the whole
  point of these axes), the parent pom at `${revision}` must still exist
  in the local repo or in Maven Central, otherwise the child build cannot
  resolve its parent. In a normal reactor build the parent is always at
  the current `${revision}`, so this is only a concern when releasing the
  GeoJSON line out-of-band against an unreleased parent.
- All four modules still build in a single reactor. The two axes control
  *what gets published with which version*, not what gets compiled together.
  Bumping `${revision}` rebuilds the entire reactor against the new geom;
  bumping `${geojson.version}` rebuilds only the three GeoJSON modules.
