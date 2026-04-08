#!/usr/bin/env bash
#
# Bump one of the two version axes.
#
# The repository tracks two versions:
#   geom    -> ${revision} property in the root pom (a CI-friendly Maven
#              placeholder used by the parent aggregator and geolatte-geom)
#   geojson -> literal versions in json-core/pom.xml, json-jackson3/pom.xml,
#              and json-jackson2/pom.xml (the three GeoJSON modules, bumped in
#              lockstep with each other but independently of geom)
#
# Usage:
#   scripts/bump-version.sh <axis> <new-version>
#
# Example: cut a new GeoJSON release without touching geom:
#   scripts/bump-version.sh geojson 1.13.0
#   mvn -pl json-core,json-jackson3,json-jackson2 -am clean install
#   mvn -pl json-core,json-jackson3,json-jackson2 deploy -P release

set -euo pipefail

if [[ $# -ne 2 ]]; then
    cat >&2 <<USAGE
Usage: $0 <axis> <new-version>

Axes:
  geom     -> ${revision} (parent aggregator + geolatte-geom)
  geojson  -> literal versions in the three GeoJSON modules

Example:
  $0 geojson 1.13.0
USAGE
    exit 1
fi

AXIS="$1"
VERSION="$2"

# Resolve repo root from this script's location so the script works regardless
# of cwd.
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

case "$AXIS" in
    geom)
        # ${revision} is a CI-friendly Maven placeholder; bump it via
        # versions:set-property. The plugin is pinned because Maven's bundled
        # 2.3 lacks set-property.
        mvn -q org.codehaus.mojo:versions-maven-plugin:2.18.0:set-property \
            -Dproperty=revision \
            -DnewVersion="$VERSION" \
            -DgenerateBackupPoms=false
        echo "Updated \${revision} to $VERSION in $REPO_ROOT/pom.xml"
        echo
        echo "Next steps (everything depends on geom, so the whole reactor rebuilds):"
        echo "  mvn clean install                            # rebuild and verify"
        echo "  mvn -pl geom deploy -P release               # publish geolatte-geom"
        ;;
    geojson)
        # The three GeoJSON modules carry literal <version> values. Maven
        # warns when a non-CI-friendly placeholder appears in <version>, so
        # we cannot use a property here. Instead, find the current version
        # from json-core/pom.xml and replace it in the three poms (their own
        # version plus the cross-module dependencies on geolatte-geojson-core).
        OLD=$(awk '
            /<artifactId>geolatte-geojson-core<\/artifactId>/ { found=1; next }
            found && /<version>/ {
                sub(/^[[:space:]]*<version>/, "")
                sub(/<\/version>.*$/, "")
                print
                exit
            }
        ' "$REPO_ROOT/json-core/pom.xml")

        if [[ -z "$OLD" ]]; then
            echo "Could not determine current geojson version from json-core/pom.xml" >&2
            exit 1
        fi

        if [[ "$OLD" == "$VERSION" ]]; then
            echo "geojson version is already $VERSION; nothing to do"
            exit 0
        fi

        for pom in json-core/pom.xml json-jackson3/pom.xml json-jackson2/pom.xml; do
            sed -i "s|<version>${OLD}</version>|<version>${VERSION}</version>|g" "$REPO_ROOT/$pom"
        done

        echo "Bumped geojson version: $OLD -> $VERSION"
        echo "  json-core/pom.xml"
        echo "  json-jackson3/pom.xml"
        echo "  json-jackson2/pom.xml"
        echo
        echo "Next steps:"
        echo "  mvn -pl json-core,json-jackson3,json-jackson2 -am clean install"
        echo "  mvn -pl json-core,json-jackson3,json-jackson2 deploy -P release"
        ;;
    *)
        echo "Unknown axis: $AXIS" >&2
        echo "Axes: geom, geojson" >&2
        exit 1
        ;;
esac
