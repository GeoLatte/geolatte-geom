# Jackson refactor — open issues

Tracking items left over after the split of `json/` into `json-core` +
`json-jackson3` + `json-jackson2`. None of these block the refactor; each
can be picked up independently.

## 1. `GeoJsonProcessingException` is orphaned

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

## 2. `Settings` is now public

**Where:** `json-core/src/main/java/org/geolatte/geom/json/Settings.java`

**Status:** previously package-private. Made public during the json-jackson2
adapter migration so that the adapter (in a different Java package) could
construct it from `GelatteGeomModule`. Minor public API expansion.

**To do:** consider whether `Settings` should be a real public configuration
type with builder semantics (the `TODO` comment in the file already
suggests this), or whether to push for an alternative that keeps it
package-private (e.g. a factory method on `Setting` that returns an
opaque `SettingsConfig` interface).

## 3. `@JsonInclude(NON_EMPTY)` was dropped from `GeoJsonFeature.getBbox()`

**Where:** `json-core/src/main/java/org/geolatte/geom/json/GeoJsonFeature.java`

**Status:** the annotation only mattered when serializing a `GeoJsonFeature`
directly through a vanilla mapper that did *not* have
`GelatteGeomModule` registered. The custom `FeatureSerializer` already
handles bbox suppression explicitly via the `SERIALIZE_FEATURE_BBOX` setting.

**To do:** confirm that no downstream code relies on the annotation. If
something does (unlikely given how it was used), document the workaround
(use `SERIALIZE_FEATURE_BBOX=true` plus a non-empty bbox).

