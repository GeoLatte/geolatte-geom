#!/usr/bin/env bash
#
# Bump one of the two version axes in the root pom.xml.
#
# The repository tracks two versions:
#   geom    -> ${revision}        used by the parent aggregator and geolatte-geom
#   geojson -> ${geojson.version} used by all three GeoJSON modules
#                                 (geolatte-geojson-core, -jackson2, -jackson3)
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
  geom     -> revision        (parent aggregator + geolatte-geom)
  geojson  -> geojson.version (the three GeoJSON modules)

Example:
  $0 geojson 1.13.0
USAGE
    exit 1
fi

AXIS="$1"
VERSION="$2"

case "$AXIS" in
    geom)     PROPERTY="revision" ;;
    geojson)  PROPERTY="geojson.version" ;;
    *)
        echo "Unknown axis: $AXIS" >&2
        echo "Axes: geom, geojson" >&2
        exit 1
        ;;
esac

# Resolve repo root from this script's location so the script works regardless
# of cwd.
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

mvn -q org.codehaus.mojo:versions-maven-plugin:2.18.0:set-property \
    -Dproperty="$PROPERTY" \
    -DnewVersion="$VERSION" \
    -DgenerateBackupPoms=false

echo "Updated $PROPERTY to $VERSION in $REPO_ROOT/pom.xml"
echo
case "$AXIS" in
    geom)
        echo "Next steps (everything depends on geom, so the whole reactor rebuilds):"
        echo "  mvn clean install                            # rebuild and verify"
        echo "  mvn -pl geom deploy -P release               # publish geolatte-geom"
        ;;
    geojson)
        echo "Next steps:"
        echo "  mvn -pl json-core,json-jackson3,json-jackson2 -am clean install"
        echo "  mvn -pl json-core,json-jackson3,json-jackson2 deploy -P release"
        ;;
esac
