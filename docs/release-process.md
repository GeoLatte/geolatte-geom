# Release process

The repository tracks two version axes in the root `pom.xml`:

| Axis      | Versioned by                        | Used by                                            |
|-----------|-------------------------------------|----------------------------------------------------|
| `geom`    | `${revision}` property              | parent aggregator + `geolatte-geom`                |
| `geojson` | literal `<version>` in each module  | `geolatte-geojson-core`, `-jackson3`, `-jackson2`  |

The GeoJSON axis uses literal `<version>` values rather than a custom property:
Maven only accepts the three CI-friendly placeholders
(`${revision}`/`${sha1}`/`${changelist}`) inside `<version>` without warning, so
the three GeoJSON poms are bumped in lockstep by `scripts/bump-version.sh`
(which rewrites the literal strings).

The `geom` axis groups the parent and `geolatte-geom` because the parent is
pom-only and exists primarily to host shared build configuration for the
geom module. The `geojson` axis groups the three GeoJSON modules because
they share the SPI in `geolatte-geojson-core`; bumping any of them in
isolation would create a configuration where the adapters depend on a
core they were not built against.

The `flatten-maven-plugin` is configured in `defaults` mode so that all
`${...}` placeholders are resolved to concrete versions in the deployed
poms (not just the standard CI-friendly `${revision}`/`${sha1}`/`${changelist}`).

Cross-module dependencies in each child pom reference the producer explicitly:

- `json-core` depends on `geolatte-geom` via `${revision}`
- `json-jackson3` and `json-jackson2` depend on `geolatte-geom` via `${revision}`
  and on `geolatte-geojson-core` via its literal version

This is what makes the two axes truly independent — bumping the GeoJSON
version rebuilds and republishes the three GeoJSON modules without forcing
a new geom release.

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
modules — they keep their own literal version. If you also want to
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

## Publishing from CI

The manual `mvn … deploy` commands above are mirrored by two GitHub Actions
workflows, which resolve the axis automatically and run the matching
`-pl`-scoped build/deploy. Both import the GPG key and deploy to Maven Central.

### Releases — `.github/workflows/publish-release.yml`

Triggered by pushing a tag, where the tag prefix selects the axis:

| Tag pattern    | Axis      | What is published                                   |
|----------------|-----------|-----------------------------------------------------|
| `geojson-v*`   | `geojson` | the three GeoJSON modules only                      |
| `geom-v*`      | `geom`    | `geolatte-geom` only                                |
| `v*`           | `both`    | the full reactor (geom + the three GeoJSON modules) |

Example — release the GeoJSON line on its own:

```
scripts/bump-version.sh geojson 2.0.1
mvn -pl json-core,json-jackson3,json-jackson2 -am clean install   # verify locally
git commit -am "Release geolatte-geojson 2.0.1"
git tag geojson-v2.0.1
git push && git push --tags
```

The workflow builds the three modules (with `-am`, so their parent and `geom`
resolve), deploys **only** the three GeoJSON modules, and creates a GitHub
Release with just those jars/poms attached. This scoping matters: a deploy that
also touched `geom` would fail if that `geom` version is already on Central.

### SNAPSHOTs — `.github/workflows/publish-snapshot.yml`

Manual trigger only (`workflow_dispatch`) with an `axis` input
(`geojson` / `geom` / `both`, default `geojson`). It verifies the version for
the selected axis ends in `-SNAPSHOT` before deploying — the `geojson` axis is
read from the literal version in `json-core/pom.xml`, the `geom` axis from
`${revision}`.

`publish-release.yml` also accepts a manual `workflow_dispatch` run with the
same `axis` input for emergency republishing (no GitHub Release is created in
that case).

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
  bumping the GeoJSON version rebuilds only the three GeoJSON modules.
