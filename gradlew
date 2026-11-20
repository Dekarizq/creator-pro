#!/bin/sh
# Gradle wrapper pengganti: kalau Gradle 8.7 belum terpasang, unduh sekali lalu jalankan.
# Di Android Studio, Gradle Sync memakai gradle-wrapper.properties di atas.
set -e
GRADLE_VERSION="8.9"
GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/gradle-$GRADLE_VERSION"
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
if [ -x "$GRADLE_HOME/bin/gradle" ]; then
  exec "$GRADLE_HOME/bin/gradle" "$@"
fi
echo "Mengunduh Gradle $GRADLE_VERSION (sekali saja)..."
mkdir -p "$(dirname "$GRADLE_HOME")"
TMP_ZIP="$(mktemp).zip"
curl -L -o "$TMP_ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
unzip -q -o "$TMP_ZIP" -d "$(dirname "$GRADLE_HOME")"
rm -f "$TMP_ZIP"
exec "$GRADLE_HOME/bin/gradle" "$@"
