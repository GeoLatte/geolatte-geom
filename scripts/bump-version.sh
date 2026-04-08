#!/usr/bin/env bash
#
# Bump the version property of one of the geolatte modules in the root pom.xml.
#
# The four publishable modules each have their own version property so that
# they can be released independently. This script wraps `mvn versions:set-property`
# with the right argument names.
#
# Modules:
#   geom            -> geom.version
#   json-core       -> geojson-core.version
#   json-jackson3   -> geojson-jackson3.version
#   json-jackson2   -> geojson-jackson2.version
#
# Usage:
#   scripts/bump-version.sh <module> <new-version>
#
# Example:
#   scripts/bump-version.sh json-jackson2 1.5.0
#   mvn -pl json-jackson2 -am clean install      # rebuild and verify
#   mvn -pl json-jackson2 deploy -P release      # publish to Maven Central

set -euo pipefail

if [[ $# -ne 2 ]]; then
    cat >&2 <<USAGE
Usage: $0 <module> <new-version>

Modules: geom, json-core, json-jackson3, json-jackson2

Example:
  $0 json-jackson2 1.5.0
USAGE
    exit 1
fi

MODULE="$1"
VERSION="$2"

case "$MODULE" in
    geom)           PROPERTY="geom.version" ;;
    json-core)      PROPERTY="geojson-core.version" ;;
    json-jackson3)  PROPERTY="geojson-jackson3.version" ;;
    json-jackson2)  PROPERTY="geojson-jackson2.version" ;;
    *)
        echo "Unknown module: $MODULE" >&2
        echo "Modules: geom, json-core, json-jackson3, json-jackson2" >&2
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
echo "Next steps:"
echo "  mvn -pl $MODULE -am clean install      # rebuild and verify"
echo "  mvn -pl $MODULE deploy -P release      # publish to Maven Central"
