#!/bin/bash
export DISPLAY=:1
cd "$(dirname "$0")"
echo "Mechi Puantaj başlatılıyor (Ekran: $DISPLAY)..."
./gradlew run
