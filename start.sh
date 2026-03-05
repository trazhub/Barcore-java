#!/bin/bash
# BardCore startup script — run from the project root
JAR="$(dirname "$0")/build/libs/BardCore-1.0-all.jar"
ENV="$(dirname "$0")/.env"

if [ ! -f "$JAR" ]; then
    echo "JAR not found. Building first..."
    ./gradlew shadowJar --console=plain
fi

# Copy .env next to the JAR so BardCore can find it
cp "$ENV" "$(dirname "$JAR")/.env" 2>/dev/null

exec java -jar "$JAR"
