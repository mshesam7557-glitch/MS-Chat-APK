#!/usr/bin/env sh
set -eu
GRADLE_VERSION="8.7"
GRADLE_DIR="${HOME}/.gradle/mschat-wrapper/gradle-${GRADLE_VERSION}"
GRADLE_BIN="${GRADLE_DIR}/bin/gradle"
if [ ! -x "$GRADLE_BIN" ]; then
  TMP_ZIP="${TMPDIR:-/tmp}/mschat-gradle-${GRADLE_VERSION}.zip"
  mkdir -p "${HOME}/.gradle/mschat-wrapper"
  if ! curl -fL --retry 2 -o "$TMP_ZIP" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"; then
    curl -fL --retry 2 -o "$TMP_ZIP" "https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  fi
  unzip -q -o "$TMP_ZIP" -d "${HOME}/.gradle/mschat-wrapper"
  rm -f "$TMP_ZIP"
fi
exec "$GRADLE_BIN" "$@"
