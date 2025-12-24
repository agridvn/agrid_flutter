#!/bin/bash

# ./scripts/bump-version.sh <new version>
# eg ./scripts/bump-version.sh "3.0.0-alpha.1"

set -eux

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $SCRIPT_DIR/..

NEW_VERSION="$1"

# Replace iOS `agridFlutterVersion` with the given version
perl -pi -e "s/agridFlutterVersion = \".*\"/agridFlutterVersion = \"$NEW_VERSION\"/" ios/Classes/AgridFlutterVersion.swift

# Replace Android `agridVersion` with the given version
perl -pi -e "s/agridVersion = \".*\"/agridVersion = \"$NEW_VERSION\"/" android/src/main/kotlin/com/agrid/flutter/AgridVersion.kt

# Replace Flutter `version` with the given version
perl -pi -e "s/^version: .*/version: $NEW_VERSION/" pubspec.yaml
