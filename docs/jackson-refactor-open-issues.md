# Jackson refactor — open issues

Tracking items left over after the split of `json/` into `json-core` +
`json-jackson3` + `json-jackson2`. None of these block the refactor; each
can be picked up independently.

## 1. `CoordinateReferenceSystems.adjustTo` does not down-convert

**Where:** `geom/src/main/java/org/geolatte/geom/crs/CoordinateReferenceSystems.java:228-249`

**Symptom:** when a 2D GeoJSON `Point` is parsed against a 3D default CRS,
the result is a 3D point with `z=0` instead of being narrowed back to 2D.
Exposed by the `@Ignore`d test
`testDeserializePointTextWithNoLimitToCrs` in
`json-jackson3/src/test/java/org/geolatte/geom/json/PointDeserializationTest.java`.

**Root cause:** `adjustTo(crs, coordinateDimension, hasM)` returns the input
CRS unchanged when `coordinateDimension <= 2`. It only adjusts dimension *up*
(2D → 3D / 4D), never down. The deserializer at
`json-core/src/main/java/org/geolatte/geom/json/GeoJsonGeometryReader.java:43-50`
calls `adjustTo` expecting it to handle both directions.

**Decision needed:** what is the desired semantics?
- (a) Strip `z`/`m` axes when narrowing (intuitive but loses information)
- (b) Reject the mismatch with a clear error
- (c) Add `IGNORE_CRS`-style flag to opt in to lossy narrowing

This is a `geom/` change and was not in the scope of the json refactor.
Once decided, remove the `@Ignore` from `PointDeserializationTest.java`.

## 2. `GeoJsonProcessingException` is orphaned

**Where:** `json-jackson3/src/main/java/org/geolatte/geom/json/jackson3/GeoJsonProcessingException.java`

**Status:** the class still extends `tools.jackson.core.JacksonException`
and is part of the public API of `json-jackson3`. After the refactor it is
no longer thrown by any code in the module — the core readers and writers
throw `GeoJsonException extends RuntimeException` (in `json-core`) and the
adapters do not wrap it.

**To do:** decide whether downstream users may catch
`GeoJsonProcessingException` directly. If yes, keep it. If no, delete it
in a follow-up. Either way, it does not exist in `json-jackson2` (which
correctly threw nothing of the kind from the start).

## 3. `Settings` is now public

**Where:** `json-core/src/main/java/org/geolatte/geom/json/Settings.java`

**Status:** previously package-private. Made public during the json-jackson2
adapter migration so that the adapter (in a different Java package) could
construct it from `GelatteGeomModule`. Minor public API expansion.

**To do:** consider whether `Settings` should be a real public configuration
type with builder semantics (the `TODO` comment in the file already
suggests this), or whether to push for an alternative that keeps it
package-private (e.g. a factory method on `Setting` that returns an
opaque `SettingsConfig` interface).

## 4. `@JsonInclude(NON_EMPTY)` was dropped from `GeoJsonFeature.getBbox()`

**Where:** `json-core/src/main/java/org/geolatte/geom/json/GeoJsonFeature.java`

**Status:** the annotation only mattered when serializing a `GeoJsonFeature`
directly through a vanilla mapper that did *not* have
`GelatteGeomModule` registered. The custom `FeatureSerializer` already
handles bbox suppression explicitly via the `SERIALIZE_FEATURE_BBOX` setting.

**To do:** confirm that no downstream code relies on the annotation. If
something does (unlikely given how it was used), document the workaround
(use `SERIALIZE_FEATURE_BBOX=true` plus a non-empty bbox).

## 5. Existing tests in `json-jackson3` not migrated to the contract

**Where:** `json-jackson3/src/test/java/org/geolatte/geom/json/*Test.java`

**Status:** the 81 existing concrete `*Test` classes in `json-jackson3`
(point/linestring/polygon/feature serialization, deserialization, CRS
handling, etc.) still live as adapter-specific tests. The new shared
contract in `json-core` has only 6 tests — enough to prove the wiring
works for both adapters but nowhere near the coverage of the existing
concrete suite.

**To do:** incrementally migrate the existing concrete tests into the
shared `AbstractGeoJsonContract` so both adapters get equal coverage.
Mechanical work; can be done test-by-test over multiple PRs.

## 6. Test fixtures `Crss` / `GeoJsonStrings` not shared

**Where:** `json-jackson3/src/test/java/org/geolatte/geom/json/Crss.java`,
`json-jackson3/src/test/java/org/geolatte/geom/json/GeoJsonStrings.java`

**Status:** both are package-private test fixture utility classes used by
the existing concrete tests in `json-jackson3`. They are tied to the
existing Jackson 3 tests and were not migrated to a shared location.

**To do:** when migrating tests to the contract (item 5), also lift these
fixtures into the `json-core` test-jar so that the Jackson 2 adapter can
use them as well.
