# Release process

The four publishable modules each have their own version property in the
root `pom.xml` so that they can be released independently:

| Module           | Property                  | Artifact id                  |
|------------------|---------------------------|------------------------------|
| `geom/`          | `geom.version`            | `geolatte-geom`              |
| `json-core/`     | `geojson-core.version`    | `geolatte-geojson-core`      |
| `json-jackson3/` | `geojson-jackson3.version`| `geolatte-geojson-jackson3`  |
| `json-jackson2/` | `geojson-jackson2.version`| `geolatte-geojson-jackson2`  |

The aggregator pom (`geolatte`) keeps its own version as `${revision}` — that
property is the default for any module that does not override it. The
`flatten-maven-plugin` is configured in `defaults` mode so that the deployed
poms have all `${...}` placeholders resolved to concrete versions, regardless
of which CI-friendly placeholder names you use.

Cross-module dependencies in each child pom reference the *producer's*
version property explicitly (e.g. `geolatte-geojson-jackson3` declares its
dependency on `geolatte-geojson-core` as `${geojson-core.version}`, not
`${project.version}`). This is what makes independent bumps work — bumping
`geojson-core.version` to a new value rebuilds both adapter modules against
the new core, but does not force them onto a new version of their own.

## Bumping a single module

Use `scripts/bump-version.sh`:

```
scripts/bump-version.sh <module> <new-version>
```

Example: cut a 1.5.0 release of the Jackson 2 adapter without touching the
other three modules.

```
scripts/bump-version.sh json-jackson2 1.5.0
mvn -pl json-jackson2 -am clean install         # rebuild and verify
mvn -pl json-jackson2 deploy -P release         # publish to Maven Central
```

The script wraps `mvn versions:set-property` (pinned to a known version of
`versions-maven-plugin`) and updates the property in the root `pom.xml`. No
backup pom files are written; the change is immediately ready to commit.

## Bumping multiple modules together

For lockstep releases (e.g. bumping everything to 1.13), run the script
multiple times:

```
scripts/bump-version.sh geom            1.13
scripts/bump-version.sh json-core       1.13
scripts/bump-version.sh json-jackson3   1.13
scripts/bump-version.sh json-jackson2   1.13
mvn clean install                                # rebuild everything
mvn deploy -P release                            # publish all four
```

If you also want to bump the parent aggregator's own version (currently
`${revision}`), update `pom.xml` directly — the script does not manage it.

## Constraints

- A module's `<parent>` reference is fixed to `${revision}` so that the
  parent pom can still be located. If you decouple a module's version from
  the parent's version (which is the whole point of these properties), the
  parent pom at the referenced version must still exist in the local repo
  or in Maven Central, otherwise dependent modules cannot find it. In the
  reactor build the parent is always at the current `${revision}`, so this
  is only a concern when releasing modules out-of-band.
- All four modules still build in a single reactor. Independent versioning
  controls *what gets published with which version*, not what gets compiled
  together. If you bump `geom.version` to 1.13, the next reactor build
  produces a new `geolatte-geom-1.13.jar` and recompiles every consumer
  against it.
